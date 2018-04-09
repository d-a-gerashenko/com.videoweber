<?php

namespace CherezWeb\ProjectBundle\Service;

use CherezWeb\ProjectBundle\AppCommandProcessor\AbstractAppCommandProcessor;
use CherezWeb\ProjectBundle\Entity\RemoteAppInterface;
use Symfony\Component\DependencyInjection\ContainerInterface as Container;

class AppCommandManager
{

    /**
     * @var Container;
     */
    private $container;
    private $processors;

    public function __construct(Container $container)
    {
        $this->container = $container;
        $this->processors = [];
    }

    public function addProcessor(AbstractAppCommandProcessor $appCommandProcessor)
    {
        if (isset($this->processors[$appCommandProcessor->getCommand()])) {
            throw new \Exception(sprintf('AppCommandProcessor for command "%s" is already registered.', $appCommandProcessor->getCommand()));
        }
        $appCommandProcessor->setContainer($this->container);
        $this->processors[$appCommandProcessor->getCommand()] = $appCommandProcessor;
    }

    /**
     * @param RemoteAppInterface $app
     * @param string $command
     * @param mixed $data
     * @return mixed
     * @throws \Exception
     */
    public function process(RemoteAppInterface $app, $command, $data)
    {
        if (!isset($this->processors[$command])) {
            throw new \Exception(sprintf('AppCommandProcessor for command "%s" doesn\'t registered.', $command));
        }
        return $this->processors[$command]->process($app, $data);
    }

    public function isChannelOnline(\CherezWeb\ProjectBundle\Entity\Channel $channel)
    {
        $limit = (new \DateTime)->sub(new \DateInterval('PT1M'));
        if ($channel->getServer()->getLastConnection() < $limit) {
            return false;
        } else {
            return true;
        }
    }
}
