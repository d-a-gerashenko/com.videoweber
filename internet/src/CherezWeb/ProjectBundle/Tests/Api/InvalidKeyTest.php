<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use \CherezWeb\ProjectBundle\Tests\ProjectWebTestCase;
use \CherezWeb\Lib\Common\ArrayPathAccess;

class InvalidKeyTest extends ProjectWebTestCase
{

    public function test()
    {
        $client = self::createClient();
        $client->request(
            'POST', '/app_api', [
            'data' => json_encode([
                'key' => 'invalide',
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

        $this->assertStringStartsWith('Exception: Can\'t find app with key "invalide".', $apiResponseDataAccess->getNodeValue('data'));
    }
}
