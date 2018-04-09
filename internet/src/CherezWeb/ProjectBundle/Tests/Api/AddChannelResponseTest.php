<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use CherezWeb\Lib\Common\ArrayPathAccess;

class AddChannelResponseTest extends AbstractAppApiTest
{

    private static $server;
    private static $client;
    private static $channel;
    private static $request;

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
        $client->setTitle('test');
        $client->setUser(self::getUser());
        self::getEntityManger()->persist($client);
        self::$client = $client;
        
        $channel = new \CherezWeb\ProjectBundle\Entity\Channel();
        $channel->setOrder(0);
        $channel->setPath('Level 1');
        $channel->setServer($server);
        $channel->setTitle('title');
        $channel->setUid(\CherezWeb\Lib\Common\RandomStringGenerator::generate());
        self::getEntityManger()->persist($channel);
        self::$channel = $channel;
        
        $request = new \CherezWeb\ProjectBundle\Entity\ChannelRequest();
        $request->setChannel($channel);
        $request->setClient($client);
        $request->setUid(\CherezWeb\Lib\Common\RandomStringGenerator::generate());
        $request->setData('request data');
        self::getEntityManger()->persist($request);
        self::$request = $request;
        
        self::getEntityManger()->flush();
    }

    public static function tearDownAfterClass()
    {
        self::getEntityManger()->refresh(self::$server);
        self::getEntityManger()->refresh(self::$client);
        self::getEntityManger()->refresh(self::$channel);
        self::getEntityManger()->refresh(self::$request);
        
        self::getEntityManger()->remove(self::$server);
        self::getEntityManger()->remove(self::$client);
        self::getEntityManger()->remove(self::$channel);
        self::getEntityManger()->remove(self::$request);
        
        self::getEntityManger()->flush();
    }

    public function getData()
    {
        return
            [
                [
                    function() {
                        return [
                            'key' => self::$server->getKey(),
                            'command' => 'add_channel_response',
                            'data' => [
                                'requestUid' => 'invalid_request_uid',
                                'data' => 'long data line'
                            ]
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        self::getEntityManger()->refresh(self::$server);
                        $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                        $this->assertSame('request_not_found', $apiResponseData->getNodeValue('data'));
                    }
                ],
                [
                    function() {
                        return [
                            'key' => self::$server->getKey(),
                            'command' => 'add_channel_response',
                            'data' => [
                                'requestUid' => self::$request->getUid(),
                                'data' => 'long data line'
                            ]
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        self::getEntityManger()->refresh(self::$server);
                        $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                        $this->assertSame('responses_added', $apiResponseData->getNodeValue('data'));
                    }
                ],
                [
                    function() {
                        return [
                            'key' => self::$server->getKey(),
                            'command' => 'add_channel_response',
                            'data' => [
                                'requestUid' => self::$request->getUid(),
                                'data' => 'long data line'
                            ]
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        self::getEntityManger()->refresh(self::$server);
                        $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                        $this->assertSame('response_exists', $apiResponseData->getNodeValue('data'));
                    }
                ],
            ];
        }
    }
    