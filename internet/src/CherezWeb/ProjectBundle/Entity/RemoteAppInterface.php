<?php
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace CherezWeb\ProjectBundle\Entity;

/**
 *
 * @author gda
 */
interface RemoteAppInterface
{
    public function getKey();
    
    public function setKey($key);
    
    public function getStateUid();
    
    public function setStateUid($stateUid);
    
    /**
	 * @return \DateTime utcdatetime
	 */
    public function getLastConnection();
    
    public function setLastConnection(\DateTime $lastConnection);
}
