<?php

namespace CherezWeb\ProjectBundle\AppCommandProcessor;

class CheckServerState extends AbstractAppCommandProcessor
{
    public function process(\CherezWeb\ProjectBundle\Entity\RemoteAppInterface $app, $data)
    {
        if (!$app instanceof \CherezWeb\ProjectBundle\Entity\Server) {
            throw new \Exception('Unexpected app type.');
        }
        /* @var $app \CherezWeb\ProjectBundle\Entity\Server */
        
        if (!is_string($data)) {
            throw new CommandDataException();
        }
        
        return ($app->getStateUid() === $data)?'equal':'different';
    }

    public function getCommand()
    {
        return 'check_server_state';
    }
}
