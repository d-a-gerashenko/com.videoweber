<?php

namespace CherezWeb\ServiceBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\Security;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Security\Core\SecurityContext;
use CherezWeb\ServiceBundle\Form\LoginType;
use CherezWeb\ServiceBundle\Entity\User;
use CherezWeb\ServiceBundle\Entity\MailAction;
use Symfony\Component\ExpressionLanguage\Expression;

class SecurityController extends Controller{

	public function loginAction(Request $request) {
        // На эту страницу идет перенаправление в случае отсутствия прав доступа,
        // следовательно могу прилетать и ajax запросы, но страница отдается не по ajax,
        // значит нужно перенаправить пользователя через js на эту страницу.
        if ($request->headers->get('X-Requested-With') === 'XMLHttpRequest') {
            return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig', array(
                    'redirectUrl' => $this->generateUrl('cherez_web_service_security_login')
            ));
        }

        if (!$this->get('security.context')->isGranted(new Expression('is_anonymous()'))) {
            return $this->redirect($this->generateUrl($this->container->getParameter('cherez_web__service__redirect__cp_index_route')));
        }

        $session = $request->getSession();

        // get the login error if there is one
		if($request->attributes->has(SecurityContext::AUTHENTICATION_ERROR)) {
            $error = $request->attributes->get(SecurityContext::AUTHENTICATION_ERROR);
        } else {
            $error = $session->get(SecurityContext::AUTHENTICATION_ERROR);
            $session->remove(SecurityContext::AUTHENTICATION_ERROR);
        }

        $loginForm = $this->createForm(
            new LoginType(),
            array('email' => $session->get(SecurityContext::LAST_USERNAME)),
            array('action' => $this->generateUrl('cherez_web_service_security_login_check'))
        );

        if ($error) {
            $loginForm->addError(new \Symfony\Component\Form\FormError('Неправильное сочетание.'));
        }

        return $this->render('CherezWebServiceBundle:Security:login.html.twig', array(
                'form' => $loginForm->createView()
        ));
    }

    /**
     * @Security("is_authenticated()")
     */
	public function loginFinalAction() {
        $this->get('session')->getFlashBag()->add(
            'notice_info',
            'Вы вошли в панель управления.'
        );

        return $this->redirect($this->generateUrl($this->container->getParameter('cherez_web__service__redirect__cp_index_route')));
    }

	public function logoutFinalAction() {
        if (!$this->get('security.context')->isGranted(new Expression('is_anonymous()'))) {
            return $this->redirect($this->generateUrl('cherez_web_service_security_logout'));
        }

        $this->get('session')->getFlashBag()->add(
            'notice_info',
            'Вы вышли из панели управления.'
        );

        return $this->redirect($this->generateUrl($this->container->getParameter('cherez_web__service__redirect__index_route')));
    }

    public function registrationAction(Request $request) {
        if (!$this->get('security.context')->isGranted(new Expression('is_anonymous()'))) {
            return $this->redirect($this->generateUrl($this->container->getParameter('cherez_web__service__redirect__cp_index_route')));
        }

        if ($this->container->getParameter('registration_restriction') !== false) {
            $this->get('session')->getFlashBag()->add(
                'notice_warning', 'Регистрация запрещена настройками сервера.'
            );
            return $this->redirect($this->generateUrl($this->container->getParameter('cherez_web__service__redirect__cp_index_route')));
        }
        
        $registrationForm = $this->createForm(new \CherezWeb\ServiceBundle\Form\RegistrationType());

        $registrationForm->handleRequest($request);

        if ($registrationForm->isValid()) {
            $formUser = $registrationForm->getData();
            /* @var $formUser User */

            $em = $this->getDoctrine()->getManager();
            /* @var $em \Doctrine\ORM\EntityManager */

            $dbUser = $em->getRepository('CherezWebServiceBundle:User')->findOneBy(array('email' => $formUser->getEmail()));
            /* @var $user User */

            if ($dbUser !== NULL && $dbUser->getIsVerified()) { // Это попытка регистрации на тот же email.
                $registrationForm->get('email')->addError(new \Symfony\Component\Form\FormError('Данный адрес уже используется.'));
            } else {
                // Если была незавершенная регистрация, то берем пользователя из базы.
                $newUser = $em->merge(($dbUser !== NULL)?$dbUser:$formUser);

                // Берем пароль из формы.
                $factory = $this->get('security.encoder_factory');
                $encoder = $factory->getEncoder($newUser);
                $password = $encoder->encodePassword($formUser->getPassword(), $newUser->getSalt());
                $newUser->setPassword($password);

                $mailAction = new MailAction();
                $em->persist($mailAction);
                $mailAction->setType(MailAction::TYPE_VERIFY);
                $mailAction->setUser($newUser);
                $mailAction->setEmail($newUser->getEmail());

                $em->flush();

                $this->get('cherez_web.default.mailer')->sendInstantMail(
                    sprintf('Завершение регистрации на "%s"', $this->container->getParameter('cherez_web__project__name')),
                    $mailAction->getEmail(),
                    'CherezWebServiceBundle:Email:registration',
                    array('mailAction' => $mailAction)
                );

                return $this->render('CherezWebServiceBundle:MailAction:sent.html.twig', array(
                        'mailAction' => $mailAction,
                        'goBackUrl' => $request->headers->get('referer'),
                ));
            }
        }

        return $this->render('CherezWebServiceBundle:Security:registration.html.twig', array(
                'form' => $registrationForm->createView(),
        ));
    }

    public function recoverAction(Request $request) {
        if (!$this->get('security.context')->isGranted(new Expression('is_anonymous()'))) {
            return $this->redirect($this->generateUrl($this->container->getParameter('cherez_web__service__redirect__cp_index_route')));
        }

        $recoverForm = $this->createForm(new \CherezWeb\ServiceBundle\Form\RecoverType());

        $recoverForm->handleRequest($request);

        if ($recoverForm->isValid()) {
            $em = $this->getDoctrine()->getManager();
            /* @var $em \Doctrine\ORM\EntityManager */

            $user = $em->getRepository('CherezWebServiceBundle:User')->findOneBy(array('email' => $recoverForm->get('email')->getData(), 'isVerified' => true));
            /* @var $user User */

            if ($user === NULL) {
                $recoverForm->get('email')->addError(new \Symfony\Component\Form\FormError('Данный адрес не зарегистрирован в системе.'));
            }

            if ($recoverForm->isValid()) {
                $mailAction = new MailAction();
                $em->persist($mailAction);
                $mailAction->setType(MailAction::TYPE_RECOVER);
                $mailAction->setUser($user);
                $mailAction->setEmail($user->getEmail());

                $em->flush();

                $this->get('cherez_web.default.mailer')->sendInstantMail(
                    sprintf('Восстановление пароля на "%s"', $this->container->getParameter('cherez_web__project__name')),
                    $mailAction->getEmail(),
                    'CherezWebServiceBundle:Email:recover',
                    array('mailAction' => $mailAction)
                );

                return $this->render('CherezWebServiceBundle:MailAction:sent.html.twig', array(
                        'mailAction' => $mailAction,
                        'goBackUrl' => $request->headers->get('referer'),
                ));
            }
        }

        return $this->render('CherezWebServiceBundle:Security:recover.html.twig', array(
                'form' => $recoverForm->createView(),
        ));
    }

    /**
     * @Security("is_authenticated()")
     */
    public function changeEmailAction(Request $request) {
        $user = $this->getUser();
        /* @var $user User */

        $formUser = clone $user;

        $changeEmailForm = $this->createForm(
            new \CherezWeb\ServiceBundle\Form\ChangeEmailType(),
            $formUser,
            array('action' => $request->getUri())
        );

        $changeEmailForm->handleRequest($request);

        if ($changeEmailForm->isValid()) {

            $factory = $this->get('security.encoder_factory');
            $encoder = $factory->getEncoder($formUser);
            $password = $encoder->encodePassword($formUser->getPassword(), $formUser->getSalt());

            if ($password !== $user->getPassword()) {
                $changeEmailForm->get('password')->addError(new \Symfony\Component\Form\FormError("Пароль указан неверно."));
            }

            if ($changeEmailForm->isValid()) {
                $em = $this->getDoctrine()->getManager();
                /* @var $em \Doctrine\ORM\EntityManager */

                $mailAction = new MailAction();
                $em->persist($mailAction);
                $mailAction->setType(MailAction::TYPE_CHANGE_MAIL);
                $mailAction->setUser($user);
                $mailAction->setEmail($formUser->getEmail());

                $em->flush();

                $this->get('cherez_web.default.mailer')->sendInstantMail(
                    sprintf('Изменение адреса электронно почты на "%s"', $this->container->getParameter('cherez_web__project__name')),
                    $mailAction->getEmail(),
                    'CherezWebServiceBundle:Email:change_email',
                    array('mailAction' => $mailAction)
                );

                return $this->render('CherezWebServiceBundle:MailAction:sent_ajax.html.twig', array(
                        'mailAction' => $mailAction,
                ));
            }
        }

        return $this->render('CherezWebServiceBundle:Security:change_email.html.twig', array(
                'form' => $changeEmailForm->createView(),
        ));
    }

    /**
     * @Security("is_authenticated()")
     */
    public function changePasswordAction(Request $request) {
        $user = $this->getUser();
        /* @var $user User */

        $currentPassword = $user->getPassword();

        $changePasswordForm = $this->createForm(
            new \CherezWeb\ServiceBundle\Form\ChangePasswordType(),
            $user,
            array('action' => $request->getUri())
        );

        $changePasswordForm->handleRequest($request);

        if ($changePasswordForm->isValid()) {
            $factory = $this->get('security.encoder_factory');
            $encoder = $factory->getEncoder($user);
            if ($encoder->encodePassword($changePasswordForm->get('currentPassword')->getData(), $user->getSalt()) !== $currentPassword) {
                $changePasswordForm->get('currentPassword')->addError(new \Symfony\Component\Form\FormError('Указан неверный пароль.'));
            }
            if ($changePasswordForm->isValid()) {
                $user->setPassword($encoder->encodePassword($user->getPassword(), $user->getSalt()));

                $em = $this->getDoctrine()->getManager();
                /* @var $em \Doctrine\ORM\EntityManager */

                $em->flush();

                $this->get('session')->getFlashBag()->add(
                    'notice_success',
                    'Пароль успешно изменен.'
                );

                return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
            }
        }

        return $this->render('CherezWebServiceBundle:Security:change_password.html.twig', array(
                'form' => $changePasswordForm->createView(),
        ));
    }

}
