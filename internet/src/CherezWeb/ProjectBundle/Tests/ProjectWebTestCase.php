<?php

namespace CherezWeb\ProjectBundle\Tests;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

abstract class ProjectWebTestCase extends WebTestCase
{

    private static $container;

    /**
     * Shortcut to return the Doctrine Registry service.
     *
     * @return \Doctrine\ORM\EntityManager
     *
     * @throws \LogicException If DoctrineBundle is not available
     */
    public static function getEntityManger()
    {
        return self::getContainer()->get('doctrine')->getManager();
    }

    public static function getContainer()
    {
        if (self::$container === null) {
            self::$kernel = self::createKernel();
            self::$kernel->boot();
            self::$container = self::$kernel->getContainer();
        }
        return self::$container;
    }

    /**
     * @return \CherezWeb\ServiceBundle\Entity\User User for tests.
     */
    public static function getUser()
    {
        return self::getEntityManger()
                ->getRepository('CherezWebServiceBundle:User')
                ->findOneBy(['email' => self::getContainer()->getParameter('cherez_web__project__email__support')]);
    }
}
