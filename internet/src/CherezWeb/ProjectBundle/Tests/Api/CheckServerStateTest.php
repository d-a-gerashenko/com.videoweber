<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use CherezWeb\Lib\Common\ArrayPathAccess;

class CheckServerStateTest extends AbstractAppApiTest
{

    private static $server;

    public static function setUpBeforeClass()
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

    public static function tearDownAfterClass()
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
                        return [
                            'key' => self::$server->getKey(),
                            'command' => 'check_server_state',
                            'data' => 'test_state'
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        self::getEntityManger()->refresh(self::$server);
                        $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                        $this->assertSame('equal', $apiResponseData->getNodeValue('data'));
                        $this->assertNotNull(self::$server->getLastConnection());
                    }
                ],
                [
                    function() {
                        return [
                            'key' => self::$server->getKey(),
                            'command' => 'check_server_state',
                            'data' => 'test_state_different'
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        self::getEntityManger()->refresh(self::$server);
                        $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                        $this->assertSame('different', $apiResponseData->getNodeValue('data'));
                    }
                ],
                [
                    function() {
                        return [
                            'key' => self::$server->getKey(),
                            'command' => 'check_server_state',
                            'data' => null
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        self::getEntityManger()->refresh(self::$server);
                        $this->assertSame('error', $apiResponseData->getNodeValue('status'));
                        $this->assertContains('CommandDataException', $apiResponseData->getNodeValue('data'));
                    }
                ],
            ];
        }
    }
    