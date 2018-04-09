<?php

namespace CherezWeb\ProjectBundle\Command;

use Doctrine\ORM\EntityManager;
use Symfony\Bundle\FrameworkBundle\Command\ContainerAwareCommand;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

class ResponseCleanupCommand extends ContainerAwareCommand
{

    protected function configure()
    {
        $this
            ->setName('cherez_web:project:response_cleanup')
            ->setDescription('Cleanup deleted responses.');
    }

    protected function execute(InputInterface $input, OutputInterface $output)
    {
        $em = $this->getContainer()->get('doctrine')->getEntityManager();
        /* @var $em EntityManager */
        
        $responsesToDelete = $em->getRepository('CherezWebProjectBundle:ChannelResponse')
            ->createQueryBuilder('cres')
            ->where('cres.request IS NULL')
            ->getQuery()
            ->setMaxResults(100)
            ->getResult();
        
        $channelRequestManager = $this->getContainer()->get('cherez_web.project.channel_request_manager');
        /* @var $channelRequestManager \CherezWeb\ProjectBundle\Service\ChannelRequestManager */
        
        foreach ($responsesToDelete as $responseToDelete) {
            /* @var $responseToDelete \CherezWeb\ProjectBundle\Entity\ChannelResponse */
            $channelRequestManager->deleteResponse($responseToDelete);
            
        }
        
        $output->writeln('done');
    }
}
