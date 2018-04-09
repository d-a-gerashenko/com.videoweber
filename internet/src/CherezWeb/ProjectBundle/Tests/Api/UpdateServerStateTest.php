<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use CherezWeb\Lib\Common\ArrayPathAccess;

class UpdateServerStateTest extends AbstractAppApiTest
{

    private static $server;

    protected function setUp()
    {
        $keyManger = self::getContainer()->get('cherez_web.project.key_manager');
        /* @var $keyManger \CherezWeb\ProjectBundle\Service\KeyManager */

        $server = new \CherezWeb\ProjectBundle\Entity\Server();
        $server->setKey($keyManger->generateUniqueKey());
        $server->setUser(self::getUser());
        $server->setLocation('test');
        $server->setStateUid('test_state');
        self::getEntityManger()->persist($server);
        self::$server = $server;
        self::getEntityManger()->flush();
    }

    protected function tearDown()
    {
        self::getEntityManger()->remove(self::$server);
        self::getEntityManger()->flush();
    }

    public function getData()
    {
        return
            [
                [
                    function() {
                        $returnData = [
                            'key' => self::$server->getKey(),
                            'command' => 'update_server_state',
                            'data' => [
                                'state_uid' => 'new_state',
                                'version' => 'new_version',
                            ]
                        ];
                        for ($i = 0; $i < 5; $i++) {
                            $returnData['data']['channels'][] = [
                                'uid' => 'channel test uid #' . $i,
                                'title' => 'channel test title #' . $i,
                            ];
                        }
                        return $returnData;
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                        $this->assertSame('server_updated', $apiResponseData->getNodeValue('data'));
                        
                        self::getEntityManger()->refresh(self::$server);
                        
                        $this->assertCount(5, self::$server->getChannels());
                        foreach (self::$server->getChannels() as $channel) {
                            $this->assertStringStartsWith('channel test uid #', $channel->getUid());
                            $this->assertStringStartsWith('channel test title #', $channel->getTitle());
                        }
                        
                        $this->assertSame(self::$server->getVersion(), 'new_version');
                        $this->assertSame(self::$server->getStateUid(), 'new_state');
                        
                    }
                ],
                [
                    function() {
                        $returnData = [
                            'key' => self::$server->getKey(),
                            'command' => 'update_server_state',
                            'data' => [
                                'state_uid' => 'new_state',
                                'version' => 'new_version',
                            ]
                        ];
                        for ($i = 0; $i < 5; $i++) {
                            $returnData['data']['channels'][] = [
                                'uid' => 'duplecated_test_uid',
                                'title' => 'channel test title #' . $i,
                            ];
                        }
                        return $returnData;
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        $this->assertSame('error', $apiResponseData->getNodeValue('status'));
                        
                    }
                ],
                [
                    function() {
                        $returnData = [
                            'key' => self::$server->getKey(),
                            'command' => 'update_server_state',
                            'data' => [
                                'state_uid' => 'new_state',
                                'version' => 'new_version',
                            ]
                        ];
                        for ($i = 0; $i < 5; $i++) {
                            $returnData['data']['channels'][] = [
                                'uid' => 't_u' . $i,
                                'title' => 'channel test title #' . $i,
                            ];
                        }
                        return $returnData;
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        $this->assertSame('error', $apiResponseData->getNodeValue('status'));
                    }
                ],
                [
                    function() {
                        $returnData = [
                            'key' => self::$server->getKey(),
                            'command' => 'update_server_state',
                            'data' => [
                                'state_uid' => 'new_state',
                                'version' => 'new_version',
                            ]
                        ];
                        for ($i = 0; $i < 5; $i++) {
                            $returnData['data']['channels'][] = [
                                'uid' => 'channel test uid #' . $i,
                            ];
                        }
                        return $returnData;
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        $this->assertSame('error', $apiResponseData->getNodeValue('status'));
                    }
                ],
                [
                    function() {
                        $returnData = [
                            'key' => self::$server->getKey(),
                            'command' => 'update_server_state',
                            'data' => [
                                'version' => 'new_version',
                            ]
                        ];
                        for ($i = 0; $i < 5; $i++) {
                            $returnData['data']['channels'][] = [
                                'uid' => 'channel test uid #' . $i,
                                'title' => 'channel test title #' . $i,
                            ];
                        }
                        return $returnData;
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        $this->assertSame('error', $apiResponseData->getNodeValue('status'));
                    }
                ],
            ];
        }
    }
    