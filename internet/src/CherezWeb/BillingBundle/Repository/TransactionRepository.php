<?php

namespace CherezWeb\BillingBundle\Repository;

use Doctrine\ORM\EntityRepository;

use CherezWeb\BillingBundle\Entity\Wallet;

class TransactionRepository extends EntityRepository {

    /**
     * @param Wallet $wallet
     * @return \Doctrine\ORM\Query
     */
    public function findAllByWalletQuery(Wallet $wallet) {
        return $this->createQueryBuilder('t')
            ->andWhere('t.wallet = :wallet')
            ->setParameter('wallet', $wallet)
            ->orderBy('t.created', 'DESC')
            ->addOrderBy('t.id', 'DESC')
            ->getQuery();
    }
    
    /**
     * @param Wallet $wallet
     * @return \Doctrine\Common\Collections\ArrayCollection
     */
    public function findAllByWallet(Wallet $wallet) {
        return $this->findAllByWalletQuery($wallet)
            ->getResult();
    }
    
}
