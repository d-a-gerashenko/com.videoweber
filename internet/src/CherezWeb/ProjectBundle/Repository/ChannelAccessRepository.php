<?php

namespace CherezWeb\ProjectBundle\Repository;

use CherezWeb\ProjectBundle\Entity\Channel;
use CherezWeb\ProjectBundle\Entity\Client;
use Doctrine\ORM\EntityRepository;

class ChannelAccessRepository extends EntityRepository
{

    public function findByChannelAndClient(Channel $channel, Client $client)
    {
        return $this
                ->createQueryBuilder('a')
                ->where('a.channel = :channel')
                ->andWhere('a.client = :client')
                ->setParameter('channel', $channel)
                ->setParameter('client', $client)
                ->getQuery()
                ->getResult();
    }

    public function deleteByChannelAndClient(Channel $channel, Client $client)
    {
        return $this
                ->createQueryBuilder('a')
                ->delete()
                ->where('a.channel = :channel')
                ->andWhere('a.client = :client')
                ->setParameter('channel', $channel)
                ->setParameter('client', $client)
                ->getQuery()
                ->execute();
    }
}
