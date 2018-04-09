<?php

namespace CherezWeb\ProjectBundle\AppCommandProcessor;

class UpdateClientState extends AbstractAppCommandProcessor
{
    public function process(\CherezWeb\ProjectBundle\Entity\RemoteAppInterface $app, $data)
    {
        if (!$app instanceof \CherezWeb\ProjectBundle\Entity\Client) {
            throw new \Exception('Unexpected app type.');
        }
        /* @var $app \CherezWeb\ProjectBundle\Entity\Client */
        
        $result = ['state_uid' => (string)$app->getStateUid()];
        
        $result['channels'] = [];
        
        foreach ($app->getChannels() as $channel) {
            /* @var $channel \CherezWeb\ProjectBundle\Entity\Channel */
            $result['channels'][] = [
                'uid' => (string)$channel->getUid(),
                'title' => (string)$channel->getTitle(),
                'order' => (int)$channel->getOrder(),
                'path' => (string)$channel->getPath(),
            ];
        }

        
        return $result;
    }

    public function getCommand()
    {
        return 'update_client_state';
    }
}
