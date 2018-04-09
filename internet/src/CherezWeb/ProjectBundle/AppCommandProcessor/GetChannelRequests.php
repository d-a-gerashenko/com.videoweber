<?php

namespace CherezWeb\ProjectBundle\AppCommandProcessor;

class GetChannelRequests extends AbstractAppCommandProcessor
{
    public function process(\CherezWeb\ProjectBundle\Entity\RemoteAppInterface $app, $data)
    {
        if (!$app instanceof \CherezWeb\ProjectBundle\Entity\Server) {
            throw new \Exception('Unexpected app type.');
        }
        /* @var $app \CherezWeb\ProjectBundle\Entity\Server */
        
        $requests = $this->getDoctrine()
            ->getRepository('CherezWebProjectBundle:ChannelRequest')
            ->getRequestsWithoutResponse($app);
        
        $result = [];
        foreach ($requests as $request) {
            /* @var $request \CherezWeb\ProjectBundle\Entity\ChannelRequest */
            $result[] = [
                'requestUid' => $request->getUid(),
                'data' => $request->getData()
            ];
        }
        
        return $result;
    }

    public function getCommand()
    {
        return 'get_channel_requests';
    }
}
