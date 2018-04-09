<?php

namespace CherezWeb\ProjectBundle\Repository;

use CherezWeb\ProjectBundle\Entity\Server;
use CherezWeb\ProjectBundle\Entity\Client;
use Doctrine\ORM\EntityRepository;

class ChannelRequestRepository extends EntityRepository
{

    public function getRequestsWithoutResponse(Server $server, $limit = 5)
    {
        return $this->createQueryBuilder('creq')
                ->join('creq.channel', 'ch')
                ->leftJoin('creq.response', 'cresp')
                ->where('ch.server = :server')
                ->andWhere('cresp IS NULL')
                ->setParameter('server', $server)
                ->orderBy('creq.created', 'DESC')
                ->setMaxResults($limit)
                ->getQuery()
                ->getResult();
    }
    
    public function getRequestsNum(Client $client)
    {
        return $this->createQueryBuilder('creq')
                ->select('count(creq.id)')
                ->where('creq.client = :client')
                ->setParameter('client', $client)
                ->getQuery()
                ->getSingleScalarResult();
    }
}
