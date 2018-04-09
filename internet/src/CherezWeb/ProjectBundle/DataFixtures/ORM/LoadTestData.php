<?php

namespace CherezWeb\ProjectBundle\DataFixtures\ORM;

use Doctrine\Common\DataFixtures\AbstractFixture;
use Doctrine\Common\DataFixtures\OrderedFixtureInterface;
use Symfony\Component\DependencyInjection\ContainerAwareInterface;
use Symfony\Component\DependencyInjection\ContainerInterface;
use Doctrine\Common\Persistence\ObjectManager;

class LoadTestData extends AbstractFixture implements OrderedFixtureInterface, ContainerAwareInterface
{

    public function setContainer(ContainerInterface $container = null)
    {
        $this->container = $container;
    }

    public function getOrder()
    {
        return 2;
    }

    public function load(ObjectManager $manager)
    {
        $keyManager = $this->container->get('cherez_web.project.key_manager');
        /* @var $keyManager \CherezWeb\ProjectBundle\Service\KeyManager */

        $user = $manager
            ->getRepository('CherezWebServiceBundle:User')
            ->findOneBy(['email' => $this->container->getParameter('cherez_web__project__email__support')]);

        $server = new \CherezWeb\ProjectBundle\Entity\Server;
        $server->setKey($keyManager->generateUniqueKey());
        $server->setLocation("Страна/Город");
        $server->setUser($user);
        $manager->persist($server);
        $manager->flush();

        $server->setLastConnection(new \DateTime);
        $server->setStateUid("demo state value");
        $server->setVersion("demo version value");
        $manager->flush();
        
        $client = new \CherezWeb\ProjectBundle\Entity\Client;
        $client->setKey($keyManager->generateUniqueKey());
        $client->setTitle("Клиент №1");
        $client->setUser($user);
        $manager->persist($client);
        $manager->flush();

        $client->setLastConnection(new \DateTime);
        $client->setStateUid("demo state value");
        $manager->flush();
        
        $channels = [];
        for ($i = 0; $i < 5; $i++) {
            $channel = new \CherezWeb\ProjectBundle\Entity\Channel;
            $channels[] = $channel;
            $channel->setServer($server);
            $channel->setOrder($i);
            $channel->setTitle("channel номер" . $i);
            $channel->setUid("channel uid #" . $i);
            $manager->persist($channel);
        }
        $channels[0]->setPath(null);
        $channels[1]->setPath("Группа1/Группа1.1/Группа1.1.1");
        $channels[2]->setPath(null);
        $channels[3]->setPath("Группа1/Группа1.1/Группа1.1.1");
        $channels[4]->setPath('Группа2');
        $manager->flush();
        
        $channelAccess1 = new \CherezWeb\ProjectBundle\Entity\ChannelAccess;
        $channelAccess1->setChannel($channels[1]);
        $channelAccess1->setClient($client);
        $manager->persist($channelAccess1);
        
        $channelAccess2 = new \CherezWeb\ProjectBundle\Entity\ChannelAccess;
        $channelAccess2->setChannel($channels[3]);
        $channelAccess2->setClient($client);
        $manager->persist($channelAccess2);
        
        $channelAccess3 = new \CherezWeb\ProjectBundle\Entity\ChannelAccess;
        $channelAccess3->setChannel($channels[4]);
        $channelAccess3->setClient($client);
        $manager->persist($channelAccess3);
        
        $manager->flush();
    }
}
