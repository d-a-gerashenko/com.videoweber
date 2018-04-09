<?php

namespace CherezWeb\BillingBundle\Service;

use Doctrine\ORM\EntityManager;

use CherezWeb\BillingBundle\Entity\Wallet;
use CherezWeb\BillingBundle\Entity\Transaction;

class Billing {
    
    private $entityManager;
    
    public function __construct(EntityManager $entityManager) {
        $this->entityManager = $entityManager;
    }
    
    /**
     * Создает транзакцию и изменяет баланс кошелька. Flush не вызывается.
     * @param Wallet $wallet
     * @param integer $sum Сумма платежа в копейках, как положительная так и отрицательная.
     * @param string $comment
     * @return Transaction
     */
    public function makeTransaction(Wallet $wallet, $sum, $comment) {
        // Списываем деньги со счета.
        $wallet->setBalance($wallet->getBalance() + $sum);

        // Создаем транзакцию.
        $transaction = new Transaction();
        $transaction->setComment($comment);
        $transaction->setWallet($wallet);
        $transaction->setSum($sum);
        $transaction->setBalanceAfter($wallet->getBalance());
        $this->entityManager->persist($transaction);
        
        return $transaction;
    }

}