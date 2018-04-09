<?php

namespace CherezWeb\ProjectBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;

class DefaultController extends Controller {

    public function indexAction() {
        return $this->render('CherezWebProjectBundle:Default:index.html.twig');
    }
    
}
