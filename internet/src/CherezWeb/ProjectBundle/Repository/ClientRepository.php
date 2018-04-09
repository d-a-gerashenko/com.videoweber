<?php

namespace CherezWeb\ProjectBundle\Repository;

use CherezWeb\ServiceBundle\Entity\User;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\QueryBuilder;

class ClientRepository extends EntityRepository
{

    /**
     * 
     * @param User $user
     * @return QueryBuilder
     */
    public function getQbFindAllByUser(User $user)
    {
        return $this
                ->createQueryBuilder('obj')
                ->where('obj.user = :user')
                ->setParameter('user', $user);
    }

    public function findAllByUser(User $user)
    {
        return $this->getQbFindAllByUser($user)
                ->getQuery()
                ->getResult();
    }

    public function findByKey($key)
    {
        return $this
                ->createQueryBuilder('obj')
                ->where('obj.key = :key')
                ->setParameter('key', $key)
                ->getQuery()
                ->getOneOrNullResult();
    }
}
