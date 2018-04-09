<?php

namespace CherezWeb\ProjectBundle\AppCommandProcessor;

class CheckChannelRequest extends AbstractAppCommandProcessor
{
    public function process(\CherezWeb\ProjectBundle\Entity\RemoteAppInterface $app, $data)
    {
        if (!$app instanceof \CherezWeb\ProjectBundle\Entity\Client) {
            throw new \Exception('Unexpected app type.');
        }
        if (!is_string($data)) {
            throw new CommandDataException();
        }
        
        $channelRequest = $this->getDoctrine()
            ->getRepository('CherezWebProjectBundle:ChannelRequest')
            ->findOneBy(['uid' => $data]);
        /* @var $channelRequest \CherezWeb\ProjectBundle\Entity\ChannelRequest */
        
        if ($channelRequest === null) {
            return ['state' => 'not_found'];
        }
        
        $channelRequest->setUpdated(new \DateTime);
        $this->getDoctrine()->getManager()->flush($channelRequest);
        
        if ($channelRequest->getResponse() === null) {
            return ['state' => 'waiting'];
        }
        
        $channelRequestManager = $this->container->get('cherez_web.project.channel_request_manager');
        /* @var $channelRequestManager \CherezWeb\ProjectBundle\Service\ChannelRequestManager */
        return [
            'state' => 'ready',
            'data' => file_get_contents($channelRequestManager->getResponseDataFile($channelRequest->getResponse())->getRealPath())
        ];
    }

    public function getCommand()
    {
        return 'check_channel_request';
    }
}
