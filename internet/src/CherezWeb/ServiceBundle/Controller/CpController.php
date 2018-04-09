<?php

namespace CherezWeb\ServiceBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\Security;
use Symfony\Component\HttpFoundation\Request;

class CpController extends Controller{
    
    /**
     * @Security("is_authenticated()")
     */
	public function indexAction(Request $request) {
        return $this->redirect($this->generateUrl($this->container->getParameter('cherez_web__service__redirect__cp_index_route')));
    }
    
    /**
     * @Security("is_authenticated()")
     */
    public function accountSettingsAction() {
        return $this->render('CherezWebServiceBundle:Cp:account_settings.html.twig');
    }
    
    /**
     * @Security("is_authenticated()")
     */
    public function billingAction() {
        return $this->render('CherezWebServiceBundle:Cp:billing.html.twig');
    }

}
