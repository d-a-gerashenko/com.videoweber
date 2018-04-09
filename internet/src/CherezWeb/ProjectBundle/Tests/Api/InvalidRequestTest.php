<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;
use \CherezWeb\Lib\Common\ArrayPathAccess;

class InvalidRequestTest extends WebTestCase
{

    public function testGET()
    {
        $client = self::createClient();
        $client->request(
            'GET', '/app_api'
        );
        $this->assertFalse($client->getResponse()->isSuccessful());
    }

    public function testPOST()
    {
        $client = self::createClient();
        $client->request(
            'POST', '/app_api'
        );
        $this->assertTrue($client->getResponse()->isSuccessful());

        $apiResponseContent = $client->getResponse()->getContent();
        $this->assertJson($apiResponseContent);
        $apiResponseData = json_decode($apiResponseContent, true);
        $this->assertTrue(is_array($apiResponseData));

        $apiResponseDataAccess = new ArrayPathAccess($apiResponseData);

        $this->assertSame($apiResponseDataAccess->getNodeValue('status'), 'error');
    }

    /**
     * @dataProvider getInvalidApiData
     */
    public function testInvalidApi($data)
    {
        $client = self::createClient();
        $client->request(
            'POST', '/app_api', ['data' => $data]
        );
        $this->assertTrue($client->getResponse()->isSuccessful());

        $apiResponseContent = $client->getResponse()->getContent();
        $this->assertJson($apiResponseContent);
        $apiResponseData = json_decode($apiResponseContent, true);
        $this->assertTrue(is_array($apiResponseData));

        $apiResponseDataAccess = new ArrayPathAccess($apiResponseData);

        $this->assertSame($apiResponseDataAccess->getNodeValue('status'), 'error');
    }

    public function getInvalidApiData()
    {
        return [
            [null],
            [''],
            [json_encode([])],
            [json_encode(null)],
            [json_encode('')],
        ];
    }
}
