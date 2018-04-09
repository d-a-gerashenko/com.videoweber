<?php

namespace CherezWeb\ProjectBundle\Service;

use CherezWeb\ProjectBundle\Entity\Channel;
use CherezWeb\ProjectBundle\Entity\Client;
use CherezWeb\ProjectBundle\Repository\ChannelAccessRepository;
use CherezWeb\ProjectBundle\Entity\ChannelAccess;
use Doctrine\ORM\EntityManager;

class ChannelAccessManager
{

    private $entityManager;

    public function __construct(EntityManager $entityManager)
    {
        $this->entityManager = $entityManager;
    }
    
    /**
     * @return ChannelAccessRepository
     */
    private function getChannelAccessRepository() {
        return $this->entityManager->getRepository('CherezWebProjectBundle:ChannelAccess'); 
    }

    /**
     * @param Channel $channel
     * @param Client $client
     * @return bool
     */
    public function checkAccess(Channel $channel, Client $client)
    {
        return (count($this->getChannelAccessRepository()->findByChannelAndClient($channel, $client)) > 0)?true:false;
    }
    
    /**
     * Flushing access entity.
     * @param Channel $channel
     * @param Client $client
     */
    public function addAccess(Channel $channel, Client $client)
    {
        if ($this->checkAccess($channel, $client) === true) {
            return;
        }
        $access = new ChannelAccess();
        $access->setChannel($channel);
        $access->setClient($client);
        $this->entityManager->persist($access);
        $this->entityManager->flush($access);
    }
    
    public function removeAccess(Channel $channel, Client $client)
    {
        $this->getChannelAccessRepository()->deleteByChannelAndClient($channel, $client);
    }
    
}
