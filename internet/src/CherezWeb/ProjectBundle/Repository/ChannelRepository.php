<?php

namespace CherezWeb\ProjectBundle\Repository;

use CherezWeb\ServiceBundle\Entity\User;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\QueryBuilder;

class ChannelRepository extends EntityRepository
{

    /**
     * 
     * @param User $user
     * @return QueryBuilder
     */
    public function getQbFindAllByUser(User $user)
    {
        return $this
                ->createQueryBuilder('c')
                ->leftJoin('c.server', 's')
                ->where('s.user = :user')
                ->setParameter('user', $user)
                ->addOrderBy('c.path')
                ->addOrderBy('c.order');
    }

    public function findAllByUser(User $user)
    {
        return $this->getQbFindAllByUser($user)
                ->getQuery()
                ->getResult();
    }
    
    public function getCountByUser(User $user)
    {
        return $this->getQbFindAllByUser($user)
            ->select('count(c)')
            ->getQuery()
            ->getSingleScalarResult();
    }
}
