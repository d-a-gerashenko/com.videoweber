<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use \CherezWeb\Lib\Common\ArrayPathAccess;

class CreateChannelRequestTest extends AbstractAppApiTest
{

    private static $server;
    private static $client;
    private static $channels;

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
            $channels[$i]->setUid(\CherezWeb\Lib\Common\RandomStringGenerator::generate());
            self::getEntityManger()->persist($channels[$i]);
        }
        $channels[0]->setPath('Level 3');
        $channels[4]->setPath('Level 5');
        self::$channels = $channels;
        
        self::getEntityManger()->flush();
        
        $channelAccessManager = self::getContainer()->get('cherez_web.project.channel_access_manager');
        /* @var $channelAccessManager ChannelAccessManager */
        unset($channels[1]);
        unset($channels[3]);
        foreach ($channels as $channel) {
            $channelAccessManager->addAccess($channel, $client);
            self::getEntityManger()->refresh($channel);
        }
        self::getEntityManger()->refresh(self::$server);
        self::getEntityManger()->refresh(self::$client);
    }

    public static function tearDownAfterClass()
    {
        
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
                        'command' => 'create_channel_request',
                        'data' => [
                            'channelUid' => self::$channels[0]->getUid(),
                            'uid' => \CherezWeb\Lib\Common\RandomStringGenerator::generate(),
                            'data' => 'long long data line',
                        ]
                    ];
                },
                function (ArrayPathAccess $apiResponseData) {
                    $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                    $this->assertSame('request_created', $apiResponseData->getNodeValue('data'));
                }
            ],
            [
                function() {
                    return [
                        'key' => self::$client->getKey(),
                        'command' => 'create_channel_request',
                        'data' => [
                            'channelUid' => self::$channels[0]->getUid(),
                            'uid' => 'duplicated_uid',
                            'data' => 'long long data line',
                        ]
                    ];
                },
                function (ArrayPathAccess $apiResponseData) {
                    $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                    $this->assertSame('request_created', $apiResponseData->getNodeValue('data'));
                }
            ],
            [
                function() {
                    return [
                        'key' => self::$client->getKey(),
                        'command' => 'create_channel_request',
                        'data' => [
                            'channelUid' => self::$channels[0]->getUid(),
                            'uid' => 'duplicated_uid',
                            'data' => 'long long data line',
                        ]
                    ];
                },
                function (ArrayPathAccess $apiResponseData) {
                    $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                    $this->assertSame('request_exists', $apiResponseData->getNodeValue('data'));
                }
            ],
            [
                function() {
                    return [
                        'key' => self::$client->getKey(),
                        'command' => 'create_channel_request',
                        'data' => [
                            'channelUid' => self::$channels[1]->getUid(),
                            'uid' => \CherezWeb\Lib\Common\RandomStringGenerator::generate(),
                            'data' => 'long long data line',
                        ]
                    ];
                },
                function (ArrayPathAccess $apiResponseData) {
                    $this->assertSame('error', $apiResponseData->getNodeValue('status'));
                    $this->assertContains('Access denied to channel_uid: ' . self::$channels[1]->getUid(), $apiResponseData->getNodeValue('data'));
                }
            ],
            [
                function() {
                    return [
                        'key' => self::$client->getKey(),
                        'command' => 'create_channel_request',
                        'data' => [
                            'channelUid' => null,
                            'uid' => \CherezWeb\Lib\Common\RandomStringGenerator::generate(),
                            'data' => 'long long data line',
                        ]
                    ];
                },
                function (ArrayPathAccess $apiResponseData) {
                    $this->assertSame('error', $apiResponseData->getNodeValue('status'));
                    $this->assertContains('Can\'t find channel with channel_uid', $apiResponseData->getNodeValue('data'));
                }
            ],
            [
                function() {
                    return [
                        'key' => self::$client->getKey(),
                        'command' => 'create_channel_request',
                        'data' => [
                            'channelUid' => 'invalid_channel_uid',
                            'uid' => \CherezWeb\Lib\Common\RandomStringGenerator::generate(),
                            'data' => 'long long data line',
                        ]
                    ];
                },
                function (ArrayPathAccess $apiResponseData) {
                    $this->assertSame('error', $apiResponseData->getNodeValue('status'));
                    $this->assertContains('Can\'t find channel with channel_uid: invalid_channel_uid', $apiResponseData->getNodeValue('data'));
                }
            ],
            [
                function() {
                    return [
                        'key' => self::$client->getKey(),
                        'command' => 'create_channel_request',
                        'data' => [
                            'channelUid' => self::$channels[0]->getUid(),
                            'uid' => '1',
                            'data' => 'long long data line',
                        ]
                    ];
                },
                function (ArrayPathAccess $apiResponseData) {
                    $this->assertSame('error', $apiResponseData->getNodeValue('status'));
                    $this->assertContains('Request UID is too short', $apiResponseData->getNodeValue('data'));
                }
            ],
        ];
    }
}
