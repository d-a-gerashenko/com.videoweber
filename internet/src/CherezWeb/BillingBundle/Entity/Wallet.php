<?php

namespace CherezWeb\BillingBundle\Entity;

use Doctrine\ORM\Mapping as ORM;

/**
 * @ORM\Table(name="chwbb_wallet")
 * @ORM\Entity
 */
class Wallet {

    public function __construct() {
        $this->balance = 0;
    }

    //--------------------------------------------------------------------------

    /**
     * @ORM\Column(type="integer")
     * @ORM\Id
     * @ORM\GeneratedValue(strategy="AUTO")
     */
    private $id;

    /**
     * Get id
     *
     * @return integer 
     */
    public function getId() {
        return $this->id;
    }

    //--------------------------------------------------------------------------

    /**
     * Остаток на балансе в копейках.
     * @ORM\Column(type="integer")
     */
    private $balance;

    /**
     * @param integer $balance Остаток на балансе в копейках.
     */
    public function setBalance($balance) {
        $this->balance = $balance;
    }

    /**
     * @return integer Остаток на балансе в копейках.
     */
    public function getBalance() {
        return $this->balance;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * @ORM\Version
     * @ORM\Column(type="integer")
     */
    private $version;
    
    public function getVersion() {
        return $this->version;
    }

}