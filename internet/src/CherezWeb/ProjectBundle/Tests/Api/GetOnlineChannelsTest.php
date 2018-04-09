<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use \CherezWeb\Lib\Common\ArrayPathAccess;

class GetOnlineChannelsTest extends AbstractAppApiTest
{

    private static $server;
    private static $server1;
    private static $client;
    private static $onlineChannelUids;

    public static function setUpBeforeClass()
    {
        $keyManger = self::getContainer()->get('cherez_web.project.key_manager');
        /* @var $keyManger \CherezWeb\ProjectBundle\Service\KeyManager */
        
        $server = new \CherezWeb\ProjectBundle\Entity\Server();
        $server->setKey($keyManger->generateUniqueKey());
        $server->setUser(self::getUser());
        $server->setLocation('test');
//        $server->setLastConnection(new \DateTime);
        self::getEntityManger()->persist($server);
        self::$server = $server;
        
        $server1 = new \CherezWeb\ProjectBundle\Entity\Server();
        $server1->setKey($keyManger->generateUniqueKey());
        $server1->setUser(self::getUser());
        $server1->setLocation('test');
        $server1->setLastConnection(new \DateTime);
        self::getEntityManger()->persist($server1);
        self::$server1 = $server1;
        
        $client = new \CherezWeb\ProjectBundle\Entity\Client();
        $client->setKey($keyManger->generateUniqueKey());
        $client->setUser(self::getUser());
        $client->setTitle('test');
        $client->setStateUid('test_state');
        self::getEntityManger()->persist($client);
        self::$client = $client;
        
        $channels = [];
        for ($i = 0; $i < 5; $i++) {
            $channels[$i] = new \CherezWeb\ProjectBundle\Entity\Channel();
            $channels[$i]->setOrder($i);
            $channels[$i]->setServer($server);
            $channels[$i]->setPath('Level 1 / Level 2' . $i);
            $channels[$i]->setTitle('Channel number #' . $i);
            $channels[$i]->setUid('uid #' . $i);
            self::getEntityManger()->persist($channels[$i]);
        }
        
        self::getEntityManger()->flush();
        
        $channelAccessManager = self::getContainer()->get('cherez_web.project.channel_access_manager');
        /* @var $channelAccessManager ChannelAccessManager */
        $channels[0]->setServer($server1);
        $channels[4]->setServer($server1);
        self::$onlineChannelUids = [$channels[4]->getUid(), $channels[0]->getUid()];
        self::getEntityManger()->flush();
        unset($channels[1]);
        unset($channels[3]);
        foreach ($channels as $channel) {
            $channelAccessManager->addAccess($channel, $client);
            self::getEntityManger()->refresh($channel);
        }
    }

    public static function tearDownAfterClass()
    {
        self::getEntityManger()->refresh(self::$server1);
        self::getEntityManger()->refresh(self::$server);
        self::getEntityManger()->refresh(self::$client);
        
        self::getEntityManger()->remove(self::$server1);
        self::getEntityManger()->remove(self::$server);
        self::getEntityManger()->remove(self::$client);
        self::getEntityManger()->flush();
    }

    public function getData()
    {
        return
        [
            [
                function() {
                    return [
                        'key' => self::$client->getKey(),
                        'command' => 'get_online_channels'
                    ];
                },
                function (ArrayPathAccess $apiResponseData) {
                    $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                    $a1 = $apiResponseData->getNodeArray('data');
                    $a2 = self::$onlineChannelUids;
                    $this->assertTrue(!array_diff($a1, $a2) && !array_diff($a2, $a1));
                }
            ],
        ];
    }
}
