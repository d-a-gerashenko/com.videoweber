<?php

namespace CherezWeb\ProjectBundle\Tests\Api;

use CherezWeb\Lib\Common\ArrayPathAccess;
use \CherezWeb\ProjectBundle\Tests\ProjectWebTestCase;

abstract class AbstractAppApiTest extends ProjectWebTestCase
{

    /**
     * @dataProvider getData
     * @param callable $dataFunction Function that returns request data.
     * @param string $assertFunstionForApiResponseData Function name to assert
     * response data. Dafault is null, in this case none function will be
     * called. Function format: public function functionName(ArrayPathAccess $apiResponseData) {...}
     */
    public final function test(callable $dataFunction, callable $assertFunstionForApiResponseData = null)
    {
        $client = self::createClient();
        $client->request(
            'POST', '/app_api', [
            'data' => json_encode(call_user_func($dataFunction))
            ]
        );

        $this->assertTrue($client->getResponse()->isSuccessful());

        $apiResponseContent = $client->getResponse()->getContent();
        $this->assertJson($apiResponseContent);
        $apiResponseData = json_decode($apiResponseContent, true);
        $this->assertTrue(is_array($apiResponseData));
        
        $apiResponseDataAccess = new ArrayPathAccess($apiResponseData);
        
        if ($assertFunstionForApiResponseData != null) {
            if (is_callable([$assertFunstionForApiResponseData, $apiResponseDataAccess])) {
                throw new \Exception('Can\'t execute assert function for api response data.');
            }
            call_user_func($assertFunstionForApiResponseData, $apiResponseDataAccess);
        }
    }

    /**
     * @return array
     */
    abstract public function getData();
}
