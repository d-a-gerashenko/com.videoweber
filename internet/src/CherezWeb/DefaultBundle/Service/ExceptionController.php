<?php

namespace CherezWeb\DefaultBundle\Service;

use Symfony\Bundle\TwigBundle\Controller\ExceptionController as DefaultExceptionController;
use Symfony\Bundle\FrameworkBundle\Templating\TemplateReference;
use Symfony\Component\HttpKernel\Exception\FlattenException;
use Symfony\Component\HttpKernel\Log\DebugLoggerInterface;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;

class ExceptionController extends DefaultExceptionController {
    
    public function showAction(Request $request, FlattenException $exception, DebugLoggerInterface $logger = null, $_format = 'html') {
        $code = $exception->getStatusCode();
        $isAjax = $request->headers->get('X-Requested-With') === 'XMLHttpRequest';
        $template = new TemplateReference('CherezWebDefaultBundle', 'Exception', $code.(($isAjax)?'_ajax':''), 'html', 'twig');
        
        // Задаем вывод ошибки по умолчанию, но только в продакшене.
        if (!$this->templateExists($template) && !$this->debug) {
            $template = new TemplateReference('CherezWebDefaultBundle', 'Exception', 'default'.(($isAjax)?'_ajax':''), 'html', 'twig');
        }
        
        if ($this->templateExists($template)) {
            $response = new Response($this->twig->render($template));
            $response->headers->set( 'X-Status-Code', 200 );
            return $response;
        }

        return parent::showAction($request, $exception, $logger, $_format);
    }
    
}
