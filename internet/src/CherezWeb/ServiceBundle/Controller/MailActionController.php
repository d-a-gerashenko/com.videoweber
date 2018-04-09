<?php

namespace CherezWeb\ServiceBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use CherezWeb\ServiceBundle\Entity\MailAction;
use Symfony\Component\HttpFoundation\Request;

class MailActionController extends Controller{
    
	public function executeAction($code, Request $request) {
        $em = $this->getDoctrine()->getManager();
        /* @var $em \Doctrine\ORM\EntityManager */
        
        $mailAction = $em->getRepository('CherezWebServiceBundle:MailAction')->findOneActiveByCode($code);
        /* @var $mailAction MailAction */
        
        if ($mailAction === NULL) {
            $this->get('session')->getFlashBag()->add(
                'notice_warning',
                'Ваша ссылка не действительна, попробуйте повторить операцию.'
            );
            return $this->redirect($this->generateUrl($this->container->getParameter('cherez_web__service__redirect__index_route')));
        }
        
        switch ($mailAction->getType()) {
            case MailAction::TYPE_VERIFY:
                $mailAction->getUser()->setIsVerified(TRUE);
                $em->remove($mailAction);
                $em->flush();
                $this->get('session')->getFlashBag()->add(
                    'notice_success',
                    'Регистрация успешно завершена.'
                );
                return $this->redirect($this->generateUrl('cherez_web_service_security_login'));
                
            case MailAction::TYPE_RECOVER:
                $recoverPasswordForm = $this->createForm(new \CherezWeb\ServiceBundle\Form\RecoverPasswordType(), $mailAction->getUser());

                $recoverPasswordForm->handleRequest($request);

                if ($recoverPasswordForm->isValid()) {
                    $factory = $this->get('security.encoder_factory');
                    $encoder = $factory->getEncoder($mailAction->getUser());
                    $password = $encoder->encodePassword($mailAction->getUser()->getPassword(), $mailAction->getUser()->getSalt());
                    $mailAction->getUser()->setPassword($password);
                    $em->remove($mailAction);
                    $em->flush();
                    $this->get('session')->getFlashBag()->add(
                        'notice_success',
                        'Пароль успешно изменен.'
                    );
                    return $this->redirect($this->generateUrl('cherez_web_service_security_login'));
                }
                return $this->render('CherezWebServiceBundle:MailAction:recover.html.twig', array('form' => $recoverPasswordForm->createView()));
            
            case MailAction::TYPE_CHANGE_MAIL:
                $user = $this->getDoctrine()->getRepository('CherezWebServiceBundle:User')->findOneBy(array('email' => $mailAction->getEmail()));
                if ($user === NULL) {
                    $mailAction->getUser()->setEmail($mailAction->getEmail());
                    $this->get('session')->getFlashBag()->add(
                        'notice_success',
                        'Адрес почты успешно изменен.'
                    );
                } else {
                    $this->get('session')->getFlashBag()->add(
                        'notice_warning',
                        sprintf('Пользователь с адресом "%s" уже зарегистрирован.', $mailAction->getEmail())
                    );
                }
                $em->remove($mailAction);
                $em->flush();
                return $this->redirect($this->generateUrl($this->container->getParameter('cherez_web__service__redirect__cp_index_route')));
        }
    }

}
