<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use \CherezWeb\Lib\Common\ArrayPathAccess;

class GetChannelRequestsTest extends AbstractAppApiTest
{

    const REQUESTS_LIMIT_PER_REQUEST = 5;
    
    private static $server1;
    private static $server2;
    private static $client;

    public static function setUpBeforeClass()
    {
        $keyManger = self::getContainer()->get('cherez_web.project.key_manager');
        /* @var $keyManger \CherezWeb\ProjectBundle\Service\KeyManager */
        
        $server1 = new \CherezWeb\ProjectBundle\Entity\Server();
        $server1->setKey($keyManger->generateUniqueKey());
        $server1->setUser(self::getUser());
        $server1->setLocation('test');
        self::getEntityManger()->persist($server1);
        self::$server1 = $server1;
        
        $server2 = new \CherezWeb\ProjectBundle\Entity\Server();
        $server2->setKey($keyManger->generateUniqueKey());
        $server2->setUser(self::getUser());
        $server2->setLocation('test');
        self::getEntityManger()->persist($server2);
        self::$server2 = $server2;
        
        $client = new \CherezWeb\ProjectBundle\Entity\Client();
        $client->setKey($keyManger->generateUniqueKey());
        $client->setUser(self::getUser());
        $client->setTitle('test');
        self::getEntityManger()->persist($client);
        self::$client = $client;
        
        $channelIndex = 0;
        
        for ($i = 0; $i < 5; $i++, $channelIndex++) {
            $channel = new \CherezWeb\ProjectBundle\Entity\Channel();
            $channel->setOrder($channelIndex);
            $channel->setServer($server1);
            $channel->setPath('test');
            $channel->setTitle('Channel number #' . $i);
            $channel->setUid(\CherezWeb\Lib\Common\RandomStringGenerator::generate());
            self::getEntityManger()->persist($channel);
        }
        
        for ($i = 0; $i < 8; $i++, $channelIndex++) {
            $channel = new \CherezWeb\ProjectBundle\Entity\Channel();
            $channel->setOrder($channelIndex);
            $channel->setServer($server2);
            $channel->setPath('test');
            $channel->setTitle('Channel number #' . $i);
            $channel->setUid(\CherezWeb\Lib\Common\RandomStringGenerator::generate());
            self::getEntityManger()->persist($channel);
        }
        
        self::getEntityManger()->flush();
        self::getEntityManger()->refresh($server1);
        self::getEntityManger()->refresh($server2);
        
        $created = new \DateTime;
        $created->modify('-1 hour');
        
        $requests = [];
        
        foreach ([$server1->getChannels()->get(0), $server1->getChannels()->get(2), $server1->getChannels()->get(3)] as $channel) {
            for ($i = 0; $i < 5; $i++) {
                $request = new \CherezWeb\ProjectBundle\Entity\ChannelRequest();
                $request->setChannel($channel);
                $request->setClient($client);
                $request->setCreated(clone $created);
                $request->setData(sprintf("data for server1 channel %s and request %s", $channel->getTitle(), count($requests)));
                $request->setUid('request uid ' . count($requests));
                self::getEntityManger()->persist($request);
                $requests[] = $request;
                
                $created->modify('+1 second');
            }
        }
        
        foreach ([$server2->getChannels()->get(2), $server2->getChannels()->get(3), $server2->getChannels()->get(5)] as $channel) {
            for ($i = 0; $i < 5; $i++) {
                $request = new \CherezWeb\ProjectBundle\Entity\ChannelRequest();
                $request->setChannel($channel);
                $request->setClient($client);
                $request->setCreated(clone $created);
                $request->setData(sprintf("data for server2 channel %s and request %s", $channel->getTitle(), count($requests)));
                $request->setUid('request uid ' . count($requests));
                self::getEntityManger()->persist($request);
                $requests[] = $request;
                
                $created->modify('+1 second');
            }
        }
        
        foreach (array_merge($server1->getChannels()->toArray(), $server2->getChannels()->toArray()) as $channel) {
            self::getEntityManger()->refresh($channel);
        }
        
        $channelRequestManager = self::getContainer()->get('cherez_web.project.channel_request_manager');
        /* @var $channelRequestManager \CherezWeb\ProjectBundle\Service\ChannelRequestManager */
        
        $response1 = $channelRequestManager->createResponseFromString($requests[11], 'response data');
        
        $response2 = $channelRequestManager->createResponseFromString($requests[20], 'response data');
        
        self::getEntityManger()->flush();
    }

    public static function tearDownAfterClass()
    {
        
        self::getEntityManger()->remove(self::$server1);
        self::getEntityManger()->remove(self::$server2);
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
                        'key' => self::$server1->getKey(),
                        'command' => 'get_channel_requests'
                    ];
                },
                function (ArrayPathAccess $apiResponseData) {
                    $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                    
                    $resultDataArray = [];
                    $requestIndex = 14;
                    foreach (array_reverse([self::$server1->getChannels()->get(0), self::$server1->getChannels()->get(2), self::$server1->getChannels()->get(3)]) as $channel) {
                        for ($i = 0; $i < 5; $i++, $requestIndex--) {
                            if ($requestIndex === 11) {
                                continue;
                            }
                            $resultDataArray[] = [
                                'requestUid' => "request uid {$requestIndex}",
                                'data' => "data for server1 channel {$channel->getTitle()} and request {$requestIndex}"

                            ];
                        }
                    }
                    $resultDataArray = array_slice($resultDataArray, 0, self::REQUESTS_LIMIT_PER_REQUEST);
                    
                    $this->assertSame(serialize($resultDataArray), serialize($apiResponseData->getNodeArray('data')));
                }
            ],
            [
                function() {
                    return [
                        'key' => self::$server2->getKey(),
                        'command' => 'get_channel_requests'
                    ];
                },
                function (ArrayPathAccess $apiResponseData) {
                    $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                    
                    $resultDataArray = [];
                    $requestIndex = 29;
                    foreach (array_reverse([self::$server2->getChannels()->get(2), self::$server2->getChannels()->get(3), self::$server2->getChannels()->get(5)]) as $channel) {
                        for ($i = 0; $i < 5; $i++, $requestIndex--) {
                            if ($requestIndex === 20) {
                                continue;
                            }
                            $resultDataArray[] = [
                                'requestUid' => "request uid {$requestIndex}",
                                'data' => "data for server2 channel {$channel->getTitle()} and request {$requestIndex}"

                            ];
                        }
                    }
                    $resultDataArray = array_slice($resultDataArray, 0, self::REQUESTS_LIMIT_PER_REQUEST);
                    
                    $this->assertSame(serialize($resultDataArray), serialize($apiResponseData->getNodeArray('data')));
                }
            ],
        ];
    }
}
