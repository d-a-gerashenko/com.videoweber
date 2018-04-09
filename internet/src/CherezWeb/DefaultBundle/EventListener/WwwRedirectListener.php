<?php

namespace CherezWeb\DefaultBundle\EventListener;

use Symfony\Component\HttpKernel\Event\GetResponseEvent;
use Symfony\Component\HttpFoundation\RedirectResponse;

class WwwRedirectListener {

    public function onKernelRequest(GetResponseEvent $event) {
        //Перенаправляем домены 2-го уровня на "www.<домен>".
        $host = mb_strtolower($event->getRequest()->getHost());
        $hostPices = mb_split('\.', $host);
        $requestUri = $event->getRequest()->getRequestUri();
        $protocol = ($event->getRequest()->isSecure())?'https':'http';

        if(count($hostPices) == 2) {
            $redirectUrl  = $protocol.'://www.'.$host.$requestUri;
            $event->setResponse(new RedirectResponse($redirectUrl));
        }
    }
}

?>
