<?php

namespace CherezWeb\ServiceBundle\DataFixtures\ORM;

use Doctrine\Common\DataFixtures\AbstractFixture;
use Doctrine\Common\DataFixtures\OrderedFixtureInterface;
use Symfony\Component\DependencyInjection\ContainerAwareInterface;
use Symfony\Component\DependencyInjection\ContainerInterface;
use Doctrine\Common\Persistence\ObjectManager;


use CherezWeb\ServiceBundle\Entity\User;

class LoadTestData extends AbstractFixture implements OrderedFixtureInterface, ContainerAwareInterface {

    public function setContainer(ContainerInterface $container = null) {
        $this->container = $container;
    }

    public function getOrder() {
        return 1;
    }

    public function load(ObjectManager $manager) {
        $factory = $this->container->get('security.encoder_factory');
		
        //----------------------------------------------------------------------
        //Пользователи
        //----------------------------------------------------------------------
        $user = $users[] = new User();
		$user->setEmail($this->container->getParameter('cherez_web__project__email__support'));
        $encoder = $factory->getEncoder($user);
		$password = $encoder->encodePassword('12345678', $user->getSalt());
		$user->setPassword($password);
		$user->setIsVerified(TRUE);
        $billing = $this->container->get('cherez_web.billing.billing');
        /* @var $billing \CherezWeb\BillingBundle\Service\Billing */
        $billing->makeTransaction($user->getWallet(), 999999000, 'Пополнение.');
        $billing->makeTransaction($user->getWallet(), 1000, 'Пополнение.');
        $billing->makeTransaction($user->getWallet(), 1000, 'Пополнение.');
        $billing->makeTransaction($user->getWallet(), 1000, 'Пополнение.');
        $billing->makeTransaction($user->getWallet(), 1000, 'Пополнение.');
        $billing->makeTransaction($user->getWallet(), -100, 'Списание.');
        $billing->makeTransaction($user->getWallet(), 1000, 'Пополнение.');
        $billing->makeTransaction($user->getWallet(), 1000, 'Пополнение.');
        $billing->makeTransaction($user->getWallet(), 1000, 'Пополнение.');
        $billing->makeTransaction($user->getWallet(), 1000, 'Пополнение.');
        $billing->makeTransaction($user->getWallet(), -300, 'Списание.');
        $billing->makeTransaction($user->getWallet(), -400, 'Списание.');
        $billing->makeTransaction($user->getWallet(), -1, 'Списание.');
        //----------------------------------------------------------------------
        foreach ($users as $user) {
            $manager->persist($user);
        }
        
        $manager->flush();
    }

}
