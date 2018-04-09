<?php

namespace CherezWeb\ProjectBundle\Controller;

use CherezWeb\Lib\Common\ArrayPathAccess;
use CherezWeb\Lib\Common\ArrayPathAccessException;
use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;

class AppApiController extends Controller
{

    public function apiRequestAction(Request $request)
    {
        $dataJsonString = $request->get('data');
        try {
            $requestData = new ArrayPathAccess(json_decode($dataJsonString, true));

            $key = $requestData->getNodeValue('key');

            if ($key === null) {
                throw new \Exception('Access key is null.');
            }

            $keyManager = $this->get('cherez_web.project.key_manager');
            /* @var $keyManager \CherezWeb\ProjectBundle\Service\KeyManager */

            $app = $keyManager->getEntityByKey($key);

            if ($app === null) {
                throw new \Exception(
                sprintf('Can\'t find app with key "%s".', $key)
                );
            }
            /* @var $app \CherezWeb\ProjectBundle\Entity\RemoteAppInterface */

            $app->setLastConnection(new \DateTime);
            $this->getDoctrine()->getManager()->flush($app);

            $command = $requestData->getNodeValue('command');

            try {
                $data = $requestData->getNode('data');
            } catch (ArrayPathAccessException $ex) {
                $data = null;
            }

            $appCommandManager = $this->get('cherez_web.project.app_command_manager');
            /* @var $appCommandManager \CherezWeb\ProjectBundle\Service\AppCommandManager */

            return new JsonResponse([
                'status' => 'success',
                'data' => $appCommandManager->process($app, $command, $data)
            ]);
        } catch (\Exception $ex) {
            return new JsonResponse([
                'status' => 'error',
                'data' => get_class($ex) . ': ' . $ex->getMessage() . PHP_EOL . $ex->getTraceAsString() . PHP_EOL .
                'Input data:' . PHP_EOL . '------------' . PHP_EOL . substr(var_export($dataJsonString, true), 0, 100) . PHP_EOL . '------------'
            ]);
        }
    }
}
