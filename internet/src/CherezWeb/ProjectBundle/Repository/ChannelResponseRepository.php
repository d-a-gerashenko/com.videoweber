<?php

namespace CherezWeb\ProjectBundle\Repository;

use Doctrine\ORM\EntityRepository;

class ChannelResponseRepository extends EntityRepository
{

    /**
     * 
     * @return int Total size of responses' data in bytes.
     */
    public function getTotalSize()
    {
        return $this->createQueryBuilder('cr')
                ->select('SUM(cr.size)')
                ->getQuery()
                ->getSingleScalarResult();
    }
}
