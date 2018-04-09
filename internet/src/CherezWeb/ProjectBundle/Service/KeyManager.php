<?php

namespace CherezWeb\ProjectBundle\Service;

use Doctrine\ORM\EntityManager;

class KeyManager {
    
    private $entityManager;
    
    public function __construct(EntityManager $entityManager) {
        $this->entityManager = $entityManager;
    }
    
    private function generateKey() {
        return \CherezWeb\Lib\Common\RandomStringGenerator::generate(50);
    }
    
    public function generateUniqueKey() {
        do {
            $key = $this->generateKey();
        } while ($this->getEntityByKey($key) !== null);
        return $key;
    }
    
    public function getEntityByKey($key) {
        $serverRepository = $this->entityManager
            ->getRepository('CherezWebProjectBundle:Server');
        $clientRepository = $this->entityManager
            ->getRepository('CherezWebProjectBundle:Client');
        $entity = $serverRepository->findByKey($key);
        if ($entity == null) {
            $entity = $clientRepository->findByKey($key);
        }
        return $entity;
    }
    
}