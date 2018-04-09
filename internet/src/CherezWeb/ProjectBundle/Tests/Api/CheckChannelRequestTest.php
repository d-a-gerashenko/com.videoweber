<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use CherezWeb\Lib\Common\ArrayPathAccess;

class CheckChannelRequestTest extends AbstractAppApiTest
{

    private static $server;
    private static $client;
    private static $channel;
    private static $requestWithoutResponse;
    private static $requestWithResponse;

    public static function setUpBeforeClass()
    {
        $keyManger = self::getContainer()->get('cherez_web.project.key_manager');
        /* @var $keyManger \CherezWeb\ProjectBundle\Service\KeyManager */
        
        $channelRequestManager = self::getContainer()->get('cherez_web.project.channel_request_manager');
        /* @var $channelRequestManager \CherezWeb\ProjectBundle\Service\ChannelRequestManager */

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
        
        $requestWithoutResponse = new \CherezWeb\ProjectBundle\Entity\ChannelRequest();
        $requestWithoutResponse->setChannel($channel);
        $requestWithoutResponse->setClient($client);
        $requestWithoutResponse->setUid(\CherezWeb\Lib\Common\RandomStringGenerator::generate());
        $requestWithoutResponse->setData('request data');
        $date = new \DateTime();
        $date->modify('-10 second');
        $requestWithoutResponse->setCreated($date);
        $requestWithoutResponse->setUpdated($requestWithoutResponse->getCreated());
        self::getEntityManger()->persist($requestWithoutResponse);
        self::$requestWithoutResponse = $requestWithoutResponse;
        
        $requestWithResponse = new \CherezWeb\ProjectBundle\Entity\ChannelRequest();
        $requestWithResponse->setChannel($channel);
        $requestWithResponse->setClient($client);
        $requestWithResponse->setUid(\CherezWeb\Lib\Common\RandomStringGenerator::generate());
        $requestWithResponse->setData('request data');
        self::getEntityManger()->persist($requestWithResponse);
        self::$requestWithResponse = $requestWithResponse;
        
        $channelRequestManager->createResponseFromString($requestWithResponse, 'response data');
        
        self::getEntityManger()->flush();
    }

    public static function tearDownAfterClass()
    {
        self::getEntityManger()->refresh(self::$server);
        self::getEntityManger()->refresh(self::$client);
        self::getEntityManger()->refresh(self::$channel);
        self::getEntityManger()->refresh(self::$requestWithoutResponse);
        self::getEntityManger()->refresh(self::$requestWithResponse);
        
        self::getEntityManger()->remove(self::$server);
        self::getEntityManger()->remove(self::$client);
        self::getEntityManger()->remove(self::$channel);
        self::getEntityManger()->remove(self::$requestWithoutResponse);
        self::getEntityManger()->remove(self::$requestWithResponse);
        
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
                            'command' => 'check_channel_request',
                            'data' => 'invalid_request_uid'
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                        $this->assertSame('not_found', $apiResponseData->getNodeValue('data.state'));
                    }
                ],
                [
                    function() {
                        return [
                            'key' => self::$client->getKey(),
                            'command' => 'check_channel_request',
                            'data' => self::$requestWithoutResponse->getUid()
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                        $this->assertSame('waiting', $apiResponseData->getNodeValue('data.state'));
                        $oldUpdated = self::$requestWithoutResponse->getUpdated();
                        self::getEntityManger()->refresh(self::$requestWithoutResponse);
                        $this->assertTrue(self::$requestWithoutResponse->getUpdated()->getTimestamp() - $oldUpdated->getTimestamp() > 0);
                    }
                ],
                [
                    function() {
                        return [
                            'key' => self::$client->getKey(),
                            'command' => 'check_channel_request',
                            'data' => self::$requestWithResponse->getUid()
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                        $this->assertSame('ready', $apiResponseData->getNodeValue('data.state'));
                        $this->assertSame('response data', $apiResponseData->getNodeValue('data.data'));
                    }
                ],
            ];
        }
    }
    