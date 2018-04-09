<?php

namespace CherezWeb\ProjectBundle\AppCommandProcessor;

use CherezWeb\Lib\Common\ArrayPathAccess;
use CherezWeb\ProjectBundle\Entity\RemoteAppInterface;
use CherezWeb\ProjectBundle\Entity\Server;

class AddChannelResponse extends AbstractAppCommandProcessor
{
    public function process(RemoteAppInterface $app, $data)
    {
        if (!$app instanceof Server) {
            throw new \Exception('Unexpected app type.');
        }
        /* @var $app Server */
        
        $dataAccess = new ArrayPathAccess($data);
        
        $em = $this->getDoctrine()->getManager();
        /* @var $em \Doctrine\ORM\EntityManager */
        
        $channelRequest = $em
            ->getRepository('CherezWebProjectBundle:ChannelRequest')
            ->findOneBy(['uid' => $dataAccess->getNodeValue('requestUid')]);
        
        if ($channelRequest === null) {
            return 'request_not_found';
        }
        
        if ($channelRequest->getResponse() !== null) {
            return 'response_exists';
        }
        
        $channelRequestManager = $this->container->get('cherez_web.project.channel_request_manager');
        /* @var $channelRequestManager \CherezWeb\ProjectBundle\Service\ChannelRequestManager */
        $channelRequestManager->createResponseFromString($channelRequest, $dataAccess->getNodeValue('data'));
        
        return 'responses_added';
    }

    public function getCommand()
    {
        return 'add_channel_response';
    }
}
