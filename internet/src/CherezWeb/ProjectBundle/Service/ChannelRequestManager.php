<?php

namespace CherezWeb\ProjectBundle\Service;

use CherezWeb\ProjectBundle\Entity\ChannelRequest;
use CherezWeb\ProjectBundle\Entity\ChannelResponse;
use Doctrine\ORM\EntityManager;
use Symfony\Component\DependencyInjection\ContainerInterface as Container;
use Symfony\Component\Filesystem\Filesystem;
use Symfony\Component\HttpFoundation\File\File;
use Symfony\Component\HttpKernel\Kernel;

class ChannelRequestManager
{

    const CREATE_MODE_COPY = 'copy';
    const CREATE_MODE_MOVE = 'move';

    /**
     * @var Kernel
     */
    private $kernel;

    /**
     * @var EntityManager
     */
    private $entityManager;

    /**
     *
     * @var Container
     */
    private $container;

    public function __construct(Kernel $kernel, Container $container)
    {
        $this->kernel = $kernel;
        $this->container = $container;
        $this->entityManager = $this->container->get('doctrine.orm.entity_manager');
    }

    public function getTempDirPath()
    {
        return $this->kernel->locateResource('@CherezWebProjectBundle/Resources/temp');
    }
    
    public function getResponseDataDirPath()
    {
        return $this->kernel->locateResource('@CherezWebProjectBundle/Resources/response_data');
    }

    public function getResponseDataFilePath(ChannelResponse $channelResponse)
    {
        return $this->kernel->locateResource('@CherezWebProjectBundle/Resources/response_data') . DIRECTORY_SEPARATOR . $this->getResponseDataFileName($channelResponse);
    }

    public function getResponseDataFile(ChannelResponse $channelResponse)
    {
        return new File($this->getResponseDataFilePath($channelResponse));
    }

    public function getResponseDataFileName(ChannelResponse $channelResponse)
    {
        return $channelResponse->getId();
    }

    public function createResponse(ChannelRequest $channelRequest, File $dataFile, $mode)
    {
        $this->entityManager->getConnection()->beginTransaction();
        try {
            $channelResponse = new ChannelResponse();
            $channelResponse->setRequest($channelRequest);
            $channelResponse->setSize($dataFile->getSize());

            $totalSize = $this->entityManager
                ->getRepository('CherezWebProjectBundle:ChannelResponse')
                ->getTotalSize();
            $totalSizeLimit = $this->container->getParameter('responses_total_size_limit');
            if ($totalSize + $channelResponse->getSize() > $totalSizeLimit) {
                throw new \Exception(sprintf('Total responses\' size limit (MB: %.2f) reached.', $totalSizeLimit / 1024 / 1024));
            }

            $this->entityManager->persist($channelResponse);
            $this->entityManager->flush($channelResponse);

            $fileSystem = new Filesystem();
            if ($mode === self::CREATE_MODE_COPY) {
                $fileSystem->copy($dataFile, $this->getResponseDataFilePath($channelResponse), true);
            } else { //self::CREATE_MODE_MOVE
                $fileSystem->rename($dataFile, $this->getResponseDataFilePath($channelResponse), true);
            }

            $this->entityManager->getConnection()->commit();
        } catch (Exception $exc) {
            $this->entityManager->getConnection()->rollBack();
            throw new \Exception('Can\'t create response.', NULL, $exc);
        }

        return $channelResponse;
    }
    
    public function createResponseFromString(ChannelRequest $channelRequest, $dataString) {
        $tmpFile = tempnam($this->getTempDirPath(), 'ch_response_data');
        file_put_contents($tmpFile, $dataString);
        $this->createResponse($channelRequest, new File($tmpFile), self::CREATE_MODE_MOVE);
    }

    public function deleteResponse(ChannelResponse $channelResponse)
    {
        $this->entityManager->getConnection()->beginTransaction();
        try {
            $this->entityManager->remove($channelResponse);

            $responseDataFilePath = $this->getResponseDataFilePath($channelResponse);
            $fileSystem = new Filesystem();
            if ($fileSystem->exists($responseDataFilePath)) {
                $fileSystem->remove($responseDataFilePath);
            }

            $this->entityManager->flush($channelResponse);
            $this->entityManager->getConnection()->commit();
        } catch (Exception $exc) {
            $this->entityManager->getConnection()->rollBack();
            throw new \Exception('Can\'t delete response.', NULL, $exc);
        }
    }
}
