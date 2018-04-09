<?php

namespace CherezWeb\ServiceBundle\Repository;

use Doctrine\ORM\EntityRepository;

use CherezWeb\ServiceBundle\Entity\MailAction;

class MailActionRepository extends EntityRepository {

    /**
     * @param string $code
     * @return MailAction
     */
    public function findOneActiveByCode($code) {
        $expireDate = new \DateTime('-1 day');
        
        // Удаляем все старые.
        $this->createQueryBuilder('al')
            ->delete()
            ->where('al.created < :expireDate')
            ->setParameter('expireDate', $expireDate, 'utcdatetime')
            ->getQuery()
            ->execute();
        
        return $this->createQueryBuilder('al')
            ->where('al.code = :code')
            ->setParameter('code', $code)
//            Закомитили, т.к. уже удалили все старые. Если удалять будем через крон, то нужно будет раскомментировать этот блок.
//            ->andWhere('al.created >= :expireDate')
//            ->setParameter('expireDate', $expireDate, 'utcdatetime')
            ->getQuery()
            ->getOneOrNullResult();
    }
    
}
