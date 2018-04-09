<?php

namespace CherezWeb\ProjectBundle\AppCommandProcessor;

class GetOnlineChannels extends AbstractAppCommandProcessor
{
    public function process(\CherezWeb\ProjectBundle\Entity\RemoteAppInterface $app, $data)
    {
        if (!$app instanceof \CherezWeb\ProjectBundle\Entity\Client) {
            throw new \Exception('Unexpected app type.');
        }
        /* @var $app \CherezWeb\ProjectBundle\Entity\Client */
        
        $appCommandManager = $this->container->get('cherez_web.project.app_command_manager');
        /* @var $appCommandManager \CherezWeb\ProjectBundle\Service\AppCommandManager */
        
        $channels = $app->getChannels();
        $onlineChannels = [];
        foreach ($channels as $channel) {
            /* @var $channel \CherezWeb\ProjectBundle\Entity\Channel */
            if ($appCommandManager->isChannelOnline($channel)) {
                $onlineChannels[] = $channel->getUid();
            }
        }
        
        return $onlineChannels;
    }

    public function getCommand()
    {
        return 'get_online_channels';
    }
}
