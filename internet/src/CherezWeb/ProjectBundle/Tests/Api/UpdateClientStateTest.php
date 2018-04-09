<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use \CherezWeb\Lib\Common\ArrayPathAccess;

class UpdateClientStateTest extends AbstractAppApiTest
{

    private static $server;
    private static $client;
    private static $channelsArray;

    public static function setUpBeforeClass()
    {
        $keyManger = self::getContainer()->get('cherez_web.project.key_manager');
        /* @var $keyManger \CherezWeb\ProjectBundle\Service\KeyManager */
        
        $server = new \CherezWeb\ProjectBundle\Entity\Server();
        $server->setKey($keyManger->generateUniqueKey());
        $server->setUser(self::getUser());
        $server->setLocation('test');
        self::getEntityManger()->persist($server);
        self::$server = $server;
        
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
        $channels[0]->setPath('Level 3');
        $channels[4]->setPath('Level 5');
        
        self::getEntityManger()->flush();
        
        $channelAccessManager = self::getContainer()->get('cherez_web.project.channel_access_manager');
        /* @var $channelAccessManager ChannelAccessManager */
        unset($channels[1]);
        unset($channels[3]);
        foreach ($channels as $channel) {
            $channelAccessManager->addAccess($channel, $client);
        }
        
        self::$channelsArray = [];
        foreach ($channels as $channel) {
            $channelAccessManager->addAccess($channel, $client);
            self::getEntityManger()->refresh($channel);
        }
        foreach ($channels as $channel) {
            self::$channelsArray[] = [
                'uid' => $channel->getUid(),
                'title' => $channel->getTitle(),
                'order' => $channel->getOrder(),
                'path' => $channel->getPath(),
            ];
        }
    }

    public static function tearDownAfterClass()
    {
        self::getEntityManger()->refresh(self::$server);
        self::getEntityManger()->refresh(self::$client);
        
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
                        'command' => 'update_client_state'
                    ];
                },
                function (ArrayPathAccess $apiResponseData) {
                    $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                    $this->assertSame(self::$client->getStateUid(), $apiResponseData->getNodeValue('data.state_uid'));
                    $this->assertSame(serialize(self::$channelsArray), serialize($apiResponseData->getNodeArray('data.channels')));
                }
            ],
        ];
    }
}
