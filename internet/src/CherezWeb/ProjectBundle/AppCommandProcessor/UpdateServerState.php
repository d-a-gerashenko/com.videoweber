<?php

namespace CherezWeb\ProjectBundle\AppCommandProcessor;

use CherezWeb\Lib\Common\ArrayPathAccess;

class UpdateServerState extends AbstractAppCommandProcessor
{
    public function process(\CherezWeb\ProjectBundle\Entity\RemoteAppInterface $app, $data)
    {
        if (!$app instanceof \CherezWeb\ProjectBundle\Entity\Server) {
            throw new \Exception('Unexpected app type.');
        }
        /* @var $app \CherezWeb\ProjectBundle\Entity\Server */
        
        $dataAccess = new ArrayPathAccess($data);
        
        $channelsData = $dataAccess->getNodeArray('channels');
        
        $channels = [];
        foreach ($app->getChannels() as $channel) {
            /* @var $channel \CherezWeb\ProjectBundle\Entity\Channel */
            $channels[$channel->getUid()] = $channel;
        }
        
        $em = $this->getDoctrine()->getManager();
        /* @var $em \Doctrine\ORM\EntityManager */
        
        // Updatind channels
        foreach ($channelsData as $channelData) {
            $channelDataAccess = new ArrayPathAccess($channelData);
            $channelUid = $channelDataAccess->getNodeValue('uid');
            if (mb_strlen($channelUid, 'UTF-8') < 5) {
                throw new CommandDataException('Channel UID is too short.');
            }
            $channelTitle = $channelDataAccess->getNodeValue('title');
            
            if (key_exists($channelUid, $channels)) {
                $channel = $channels[$channelUid];
                /* @var $channel \CherezWeb\ProjectBundle\Entity\Channel */
                $channel->setTitle($channelTitle);
                foreach ($channel->getAccesses() as $access) {
                    /* @var $access \CherezWeb\ProjectBundle\Entity\ChannelAccess */
                    $access->getClient()->updateStateUid();
                }
                unset($channels[$channelUid]);
            } else {
                $newChannel = new \CherezWeb\ProjectBundle\Entity\Channel();
                $newChannel->setOrder(
                    $em->getRepository('CherezWebProjectBundle:Channel')
                        ->getCountByUser($app->getUser())
                );
                $newChannel->setServer($app);
                $newChannel->setTitle($channelTitle);
                $newChannel->setUid($channelUid);
                $this->getDoctrine()->getManager()->persist($newChannel);
            }
        }
        
        // Delete channels
        foreach ($channels as $channel) {
            /* @var $channel \CherezWeb\ProjectBundle\Entity\Channel */
            foreach ($channel->getAccesses() as $access) {
                /* @var $access \CherezWeb\ProjectBundle\Entity\ChannelAccess */
                $access->getClient()->updateStateUid();
            }
            $em->remove($channel);
        }
        
        $app->setStateUid($dataAccess->getNodeValue('state_uid'));
        $app->setVersion($dataAccess->getNodeValue('version'));
        
        $em->flush();
        
        return 'server_updated';
    }

    public function getCommand()
    {
        return 'update_server_state';
    }
}
