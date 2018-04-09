<?php

namespace CherezWeb\ProjectBundle\AppCommandProcessor;

use CherezWeb\ProjectBundle\Entity\RemoteAppInterface;
use Symfony\Component\DependencyInjection\ContainerAware;
use Doctrine\Bundle\DoctrineBundle\Registry;

abstract class AbstractAppCommandProcessor extends ContainerAware {

    /**
     * @throws \Exception
     * @return mixed
     */
    abstract public function process(RemoteAppInterface $app, $data);
    
    /**
     * @return string Команда, которая обрабатывается этим обработчиком.
     */
    abstract public function getCommand();

    /**
     * Shortcut to return the Doctrine Registry service.
     *
     * @return Registry
     *
     * @throws \LogicException If DoctrineBundle is not available
     */
    public function getDoctrine() {
        if (!$this->container->has('doctrine')) {
            throw new \LogicException('The DoctrineBundle is not registered in your application.');
        }

        return $this->container->get('doctrine');
    }

}
