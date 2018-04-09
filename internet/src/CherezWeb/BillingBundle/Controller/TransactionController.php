<?php

namespace CherezWeb\BillingBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpFoundation\Request;
use CherezWeb\BillingBundle\Entity\Wallet;

class TransactionController extends Controller {
    
    /**
     * Экшн используется для форвардинга из экшена приложения.
     * @param Request $request
     * @return \Symfony\Component\HttpFoundation\Response
     */
    public function listAction(Request $request, Wallet $wallet) {
        $query = $this->getDoctrine()->getManager()
            ->getRepository('CherezWebBillingBundle:Transaction')
            ->findAllByWalletQuery($wallet);
        
        $paginator  = $this->get('knp_paginator');
        $pagination = $paginator->paginate(
            $query,
            $request->query->get('page', 1), // Номер страницы.
            10 // Элементов на странице.
        );
        
        return $this->render('CherezWebBillingBundle:Transaction:list.html.twig', array(
            'pagination' => $pagination,
        ));
    }

}