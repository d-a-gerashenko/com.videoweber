<?php

namespace CherezWeb\ProjectBundle\AppCommandProcessor;


class CreateChannelRequest extends AbstractAppCommandProcessor
{
    public function process(\CherezWeb\ProjectBundle\Entity\RemoteAppInterface $app, $data)
    {
        if (!$app instanceof \CherezWeb\ProjectBundle\Entity\Client) {
            throw new \Exception('Unexpected app type.');
        }
        $dataAccess = new \CherezWeb\Lib\Common\ArrayPathAccess($data);
        
        $channelUid = $dataAccess->getNodeValue('channelUid');
        $requestUid = $dataAccess->getNodeValue('uid');
        $requestData = $dataAccess->getNodeValue('data');
        
        if (mb_strlen($requestUid, 'UTF-8') < 5) {
            throw new CommandDataException('Request UID is too short.');
        }
        
        $oldChannelRequest = $this->getDoctrine()
            ->getRepository('CherezWebProjectBundle:ChannelRequest')
            ->findOneBy(['uid' => $requestUid]);
        
        if ($oldChannelRequest !== null) {
            return 'request_exists';
        }
        
        $channel = $this->getDoctrine()
            ->getRepository('CherezWebProjectBundle:Channel')
            ->findOneBy(['uid' => $channelUid]);
        
        if ($channel === null) {
            throw new \Exception(
                sprintf('Can\'t find channel with channel_uid: %s.', $channelUid)
            );
        }
        
        $channelAccessManager = $this->container->get('cherez_web.project.channel_access_manager');
        /* @var $channelAccessManager \CherezWeb\ProjectBundle\Service\ChannelAccessManager */
        if ($channelAccessManager->checkAccess($channel, $app) !== true) {
            throw new \Exception(
                sprintf('Access denied to channel_uid: %s.', $channelUid)
            );
        }
        
        $clientRequestsNum = $this->getDoctrine()
            ->getRepository('CherezWebProjectBundle:ChannelRequest')
            ->getRequestsNum($app);
        $clientRequestsNumLimit = $this->container->getParameter('client_requests_limit');
        if ($clientRequestsNum >= $clientRequestsNumLimit) {
            throw new \Exception(sprintf(
                'Requests\' num limit (%s) for this client (%s) reached.',
                $clientRequestsNumLimit,
                $app->getKey()
            ));
        }
        
        $channelRequest = new \CherezWeb\ProjectBundle\Entity\ChannelRequest;
        $channelRequest->setClient($app);
        $channelRequest->setData($requestData);
        $channelRequest->setUid($requestUid);
        $channelRequest->setChannel($channel);
        
        $em = $this->getDoctrine()->getManager();
        $em->persist($channelRequest);
        $em->flush($channelRequest);
        
        return 'request_created';
    }

    public function getCommand()
    {
        return 'create_channel_request';
    }
}
