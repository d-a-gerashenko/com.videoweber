<?php

namespace CherezWeb\BillingBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;

use CherezWeb\BillingBundle\Entity\Payment;
use CherezWeb\BillingBundle\Entity\Wallet;

use CherezWeb\BillingBundle\Form\MakePaymentType;

class PaymentController extends Controller {
    
    /**
     * Этот action используется для forwarding-а из контроллеров приложения, напрямую не запускается.
     * @param Request $request
     * @param Wallet $wallet
     * @return Response
     */
    public function makeAction(Request $request, Wallet $wallet) {
        $newPayment = new Payment();
        $newPayment->setComment(sprintf('Пополнение баланса кошелька #%s через систему "РОБОКАССА".', $wallet->getId()));
        $newPayment->setProvider(Payment::PROVIDER_ROBOKASSA);
        $newPayment->setReturnURL($request->headers->get('referer'));
        $newPayment->setWallet($wallet);
        
        $makePaymentForm = $this->createForm(
            new MakePaymentType(),
            $newPayment,
            array('action' => $request->getUri())
        );
        
        $makePaymentForm->handleRequest($request);
        
        if ($makePaymentForm->isValid()) {
            $em = $this->getDoctrine()->getManager();
            $newPayment->setSum($newPayment->getSum() * 100); // Переводим в копейки.
            $em->persist($newPayment);
            $em->flush();
            
            $outSum = $newPayment->getSum() / 100; // Переводим рубли.
            $robokassaMerchanLogin = $this->container->getParameter('cherez_web__billing__robokassa__merchan_login');
            $robokassaMerchanPass1 = $this->container->getParameter('cherez_web__billing__robokassa__merchan_pass_1');
            
            $mySecretCode = md5($robokassaMerchanPass1.$newPayment->getId().$this->container->getParameter('cherez_web__billing__secret'));

            $redirectUrl = 'https://auth.robokassa.ru/Merchant/Index.aspx?' . http_build_query(array(
                'MrchLogin' => $robokassaMerchanLogin,
                'OutSum' => $outSum,
                'InvId' => $newPayment->getId(),
                'Desc' => $newPayment->getComment(),
                'SignatureValue' => md5(implode(':', array(
                    $robokassaMerchanLogin,
                    $outSum,
                    $newPayment->getId(),
                    $robokassaMerchanPass1,
                )).':shpa=' . $mySecretCode),
                //'IncCurrLabel' => 'RUR',
                'Culture' => 'ru',
                'shpa' => $mySecretCode,
            ));
            // TODO: На самом деле лучше использовать не редирект, а открытие
            // новой страницы, для этого лучше создать еще один twig шаблон,
            // который будет через ajax возвращать js скрипт выполняющий эту операцию.
            return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig', array('redirectUrl' => $redirectUrl));
        }
        
        return $this->render('CherezWebBillingBundle:Payment:make.html.twig', array(
            'form' => $makePaymentForm->createView(),
        ));
    }
    
    public function resultAction(Request $request) {
        $robokassaMerchanPass2 = $this->container->getParameter('cherez_web__billing__robokassa__merchan_pass_2');
        
        $outSum = $request->get('OutSum');
        $invId = $request->get('InvId');
        $signatureValue = $request->get('SignatureValue');
        $mySecretCode = $request->get('shpa');
        
        if ($outSum === NULL || $invId === NULL || $signatureValue === NULL || $mySecretCode === NULL) {
            return new Response('Указаны не все данные.');
        }
        
        // Выполняем регистронезависимое сравнение md5.
        if (strcasecmp($signatureValue, md5(
            implode(':', array($outSum, $invId, $robokassaMerchanPass2)).
            ':shpa='.
            $mySecretCode
        ))) {
            return new Response('Не совпадает md5.');
        }
        
        $em = $this->getDoctrine()->getManager();
                
        $payment = $em->getRepository('CherezWebBillingBundle:Payment')->find($invId);
        /* @var $payment Payment */
        
        if ($payment === NULL) {
            return new Response(sprintf('Не найден платеж #%s.', $invId));
        }
        
        $this->makeTransactionIfNeed($payment, $outSum);
        
        $payment->setFinalized(new \DateTime());
        
        $em->flush();
        
        return new Response('OK'.$payment->getId());
    }
    
    public function successAction(Request $request) {
        $robokassaMerchanPass1 = $this->container->getParameter('cherez_web__billing__robokassa__merchan_pass_1');
        
        $outSum = $request->get('OutSum');
        $invId = $request->get('InvId');
        $signatureValue = $request->get('SignatureValue');
        $culture = $request->get('Culture');
        $mySecretCode = $request->get('shpa');
        
        if ($outSum === NULL || $invId === NULL || $signatureValue === NULL || $culture === NULL || $mySecretCode === NULL) {
            return $this->render('CherezWebBillingBundle:Payment:fail.html.twig', array(
                'message' => 'Указаны не все данные платежа.'
            ));
        }
        
        // Выполняем регистронезависимое сравнение md5.
        if (strcasecmp($signatureValue, md5(
            implode(':', array($outSum, $invId, $robokassaMerchanPass1)).
            ':shpa='.
            $mySecretCode
        ))) {
            return $this->render('CherezWebBillingBundle:Payment:fail.html.twig', array(
                'message' => 'Не совпадает md5 платежа.'
            ));
        }
        
        $em = $this->getDoctrine()->getManager();
                
        $payment = $em->getRepository('CherezWebBillingBundle:Payment')->find($invId);
        /* @var $payment Payment */
        
        
        if ($payment === NULL) {
            return $this->render('CherezWebBillingBundle:Payment:fail.html.twig', array(
                'message' => sprintf('Не найден платеж #%s.', $invId)
            ));
        }
        
        $this->makeTransactionIfNeed($payment, $outSum);
        
        return $this->render('CherezWebBillingBundle:Payment:success.html.twig', array(
            'payment' => $payment
        ));
    }
    
    /**
     * Функция минимизирующая возможность двойных транзакций на один платеж.
     * @param Payment $payment
     * @param type $sum Рубли.
     */
    private function makeTransactionIfNeed(Payment $payment, $sum) {
        // Обновляем $payment, так как нужно минимизировать возможность двойной транзакции на один платеж.
        $em = $this->getDoctrine()->getManager();
        $em->refresh($payment);
        if ($payment->getResult() !== Payment::RESULT_SUCCESS) {
            $billing = $this->get('cherez_web.billing.billing');
            /* @var $billing \CherezWeb\BillingBundle\Service\Billing */

            $trasaction = $billing->makeTransaction(
                $payment->getWallet(),
                (int)($sum * 100), // Переводим в копейки.
                $payment->getComment()
            );

            $payment->setTransaction($trasaction);
            $payment->setResult(Payment::RESULT_SUCCESS);
            $em->flush();
        }
    }
    
    public function failAction(Request $request) {
        $robokassaMerchanPass1 = $this->container->getParameter('cherez_web__billing__robokassa__merchan_pass_1');
        
        $outSum = $request->get('OutSum');
        $invId = $request->get('InvId');
        $culture = $request->get('Culture');
        $mySecretCode = $request->get('shpa');
        
        if ($outSum === NULL || $invId === NULL || $culture === NULL || $mySecretCode === NULL) {
            return $this->render('CherezWebBillingBundle:Payment:fail.html.twig', array(
                'message' => 'Указаны не все данные.'
            ));
        }
        
        if ($mySecretCode !== md5($robokassaMerchanPass1.$invId.$this->container->getParameter('cherez_web__billing__secret'))) {
            return $this->render('CherezWebBillingBundle:Payment:fail.html.twig', array(
                'message' => 'Не совпадает секретный код.'
            ));
        }
        
        $em = $this->getDoctrine()->getManager();
                
        $payment = $em->getRepository('CherezWebBillingBundle:Payment')->find($invId);
        /* @var $payment Payment */
        
        
        if ($payment === NULL) {
            return $this->render('CherezWebBillingBundle:Payment:fail.html.twig', array(
                'message' => sprintf('Не найден платеж #%s.', $invId)
            ));
        }
        
        $em->refresh($payment);
        if ($payment->getResult() === Payment::RESULT_SUCCESS) {
            return $this->render('CherezWebBillingBundle:Payment:success.html.twig', array(
                'payment' => $payment
            ));
        }
        
        $payment->setResult(Payment::RESULT_FAIL);
        $em->flush();
        
        return $this->render('CherezWebBillingBundle:Payment:fail.html.twig', array(
            'message' => sprintf('Платеж #%s отменен.', $invId),
            'payment' => $payment
        ));
    }

}