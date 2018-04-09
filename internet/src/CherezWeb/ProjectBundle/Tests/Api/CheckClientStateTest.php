<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use CherezWeb\Lib\Common\ArrayPathAccess;

class CheckClientStateTest extends AbstractAppApiTest
{

    private static $client;

    public static function setUpBeforeClass()
    {
        $keyManger = self::getContainer()->get('cherez_web.project.key_manager');
        /* @var $keyManger \CherezWeb\ProjectBundle\Service\KeyManager */

        $client = new \CherezWeb\ProjectBundle\Entity\Client();
        $client->setKey($keyManger->generateUniqueKey());
        $client->setUser(self::getUser());
        $client->setTitle('test');
        $client->setStateUid('test_state');
        self::getEntityManger()->persist($client);
        self::getEntityManger()->flush();
        self::$client = $client;
    }

    public static function tearDownAfterClass()
    {
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
                            'command' => 'check_client_state',
                            'data' => 'test_state'
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        self::getEntityManger()->refresh(self::$client);
                        $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                        $this->assertSame('equal', $apiResponseData->getNodeValue('data'));
                        $this->assertNotNull(self::$client->getLastConnection());
                    }
                ],
                [
                    function() {
                        return [
                            'key' => self::$client->getKey(),
                            'command' => 'check_client_state',
                            'data' => 'test_state_different'
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        self::getEntityManger()->refresh(self::$client);
                        $this->assertSame('success', $apiResponseData->getNodeValue('status'));
                        $this->assertSame('different', $apiResponseData->getNodeValue('data'));
                    }
                ],
                [
                    function() {
                        return [
                            'key' => self::$client->getKey(),
                            'command' => 'check_client_state',
                            'data' => null
                        ];
                    },
                    function (ArrayPathAccess $apiResponseData) {
                        self::getEntityManger()->refresh(self::$client);
                        $this->assertSame('error', $apiResponseData->getNodeValue('status'));
                        $this->assertContains('CommandDataException', $apiResponseData->getNodeValue('data'));
                    }
                ],
            ];
        }
    }
    