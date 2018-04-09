<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use \CherezWeb\ProjectBundle\Tests\ProjectWebTestCase;
use \CherezWeb\Lib\Common\ArrayPathAccess;

class InvalidCommandTest extends ProjectWebTestCase
{

    private static $client;
    private static $server;

    public static function setUpBeforeClass()
    {
        $keyManger = self::getContainer()->get('cherez_web.project.key_manager');
        /* @var $keyManger \CherezWeb\ProjectBundle\Service\KeyManager */


        $client = new \CherezWeb\ProjectBundle\Entity\Client();
        $client->setKey($keyManger->generateUniqueKey());
        $client->setUser(self::getUser());
        $client->setTitle('test');
        self::getEntityManger()->persist($client);
        self::$client = $client;

        $server = new \CherezWeb\ProjectBundle\Entity\Server();
        $server->setKey($keyManger->generateUniqueKey());
        $server->setUser(self::getUser());
        $server->setLocation('test');
        self::getEntityManger()->persist($server);
        self::$server = $server;
        
        self::getEntityManger()->flush();
    }

    public static function tearDownAfterClass()
    {
        self::getEntityManger()->remove(self::$client);
        self::getEntityManger()->remove(self::$server);
        self::getEntityManger()->flush();
    }

    public function testInvalidClientCommand()
    {
        $client = self::createClient();
        $client->request(
            'POST', '/app_api', [
            'data' => json_encode([
                'key' => self::$client->getKey(),
                'command' => 'invalid_command_name',
            ])
            ]
        );
        $this->assertTrue($client->getResponse()->isSuccessful());
        $apiResponseContent = $client->getResponse()->getContent();
        $this->assertJson($apiResponseContent);
        $apiResponseData = json_decode($apiResponseContent, true);
        $this->assertTrue(is_array($apiResponseData));

        $apiResponseDataAccess = new ArrayPathAccess($apiResponseData);

        $this->assertSame($apiResponseDataAccess->getNodeValue('status'), 'error');
        $this->assertStringStartsWith('Exception: AppCommandProcessor for command "invalid_command_name" doesn\'t registered.', $apiResponseDataAccess->getNodeValue('data'));
    }

    public function testInvalidServerCommand()
    {
        $client = self::createClient();
        $client->request(
            'POST', '/app_api', [
            'data' => json_encode([
                'key' => self::$server->getKey(),
                'command' => 'invalid_command_name',
            ])
            ]
        );
        $this->assertTrue($client->getResponse()->isSuccessful());
        $apiResponseContent = $client->getResponse()->getContent();
        $this->assertJson($apiResponseContent);
        $apiResponseData = json_decode($apiResponseContent, true);
        $this->assertTrue(is_array($apiResponseData));

        $apiResponseDataAccess = new ArrayPathAccess($apiResponseData);

        $this->assertSame($apiResponseDataAccess->getNodeValue('status'), 'error');
        $this->assertStringStartsWith('Exception: AppCommandProcessor for command "invalid_command_name" doesn\'t registered.', $apiResponseDataAccess->getNodeValue('data'));
    }
}
