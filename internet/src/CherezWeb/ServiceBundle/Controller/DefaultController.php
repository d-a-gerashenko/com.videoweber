<?php

namespace CherezWeb\ServiceBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;

class DefaultController extends Controller {
    
    public function termsOfServiceAction() {
        return $this->render('CherezWebServiceBundle:Default:terms_of_service.html.twig');
    }
    
}
