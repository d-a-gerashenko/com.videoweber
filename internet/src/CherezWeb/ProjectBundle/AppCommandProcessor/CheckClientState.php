<?php

namespace CherezWeb\ProjectBundle\AppCommandProcessor;

class CheckClientState extends AbstractAppCommandProcessor
{
    public function process(\CherezWeb\ProjectBundle\Entity\RemoteAppInterface $app, $data)
    {
        if (!$app instanceof \CherezWeb\ProjectBundle\Entity\Client) {
            throw new \Exception('Unexpected app type.');
        }
        /* @var $app \CherezWeb\ProjectBundle\Entity\Client */
        
        if (!is_string($data)) {
            throw new CommandDataException();
        }
        
        return ($app->getStateUid() === $data)?'equal':'different';
    }

    public function getCommand()
    {
        return 'check_client_state';
    }
}
