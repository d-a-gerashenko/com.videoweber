<?php

namespace CherezWeb\ServiceBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\Security;
use Symfony\Component\HttpFoundation\Request;
use CherezWeb\BillingBundle\Entity\Wallet;

class BillingController extends Controller{
    
    /**
     * @Security("is_authenticated()")
     */
	public function transactionListAction(Request $request) {
        $wallet = $this->getDoctrine()->getManager()
            ->getRepository('CherezWebServiceBundle:User')->getFlushedWallet($this->getUser());
        /* @var $wallet Wallet */
        return $this->forward(
            'CherezWebBillingBundle:Transaction:list',
            array(
                'request' => $request,
                'wallet' => $wallet
            )
        );
    }
    
    /**
     * @Security("is_authenticated()")
     */
	public function makePaymentAction(Request $request) {
        $wallet = $this->getDoctrine()->getManager()
            ->getRepository('CherezWebServiceBundle:User')->getFlushedWallet($this->getUser());
        /* @var $wallet Wallet */
        return $this->forward(
            'CherezWebBillingBundle:Payment:make',
            array(
                'request' => $request,
                'wallet' => $wallet
            )
        );
    }

}
