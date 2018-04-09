<?php

namespace CherezWeb\ProjectBundle\Entity;

use Doctrine\ORM\Mapping as ORM;
use Doctrine\Common\Collections\ArrayCollection;

/**
 * @ORM\Table(name="chwpb_client")
 * @ORM\Entity(repositoryClass="CherezWeb\ProjectBundle\Repository\ClientRepository")
 */
class Client implements RemoteAppInterface {
    
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
     * @ORM\Column(type="string", length=100, nullable=true)
     */
    private $stateUid;

    public function setStateUid($stateUid) {
        $this->stateUid = $stateUid;
    }

    public function getStateUid() {
        return $this->stateUid;
    }
    
    public function updateStateUid()
    {
        $this->stateUid = \CherezWeb\Lib\Common\RandomStringGenerator::generate(50);
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * @ORM\Column(name="_key", type="string", length=100, unique=true)
     */
    private $key;

    public function setKey($key) {
        $this->key = $key;
    }

    public function getKey() {
        return $this->key;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * @ORM\Column(type="string", length=100)
     */
    private $title;

    public function setTitle($title) {
        $this->title = $title;
    }

    public function getTitle() {
        return $this->title;
    }

    //--------------------------------------------------------------------------
    
    /**
     * utcdatetime
     * @var \DateTime
     * @ORM\Column(type="utcdatetime", nullable=true)
     */
    protected $lastConnection;

	public function setLastConnection(\DateTime $lastConnection) {
		$this->lastConnection = $lastConnection;
	}

    /**
	 * @return \DateTime utcdatetime
	 */
	public function getLastConnection() {
        if ($this->lastConnection !== NULL) {
            $this->lastConnection->setTimeZone(new \DateTimeZone(date_default_timezone_get()));
        }
		return $this->lastConnection;
	}
    
    //--------------------------------------------------------------------------

    /**
     * @var \CherezWeb\ServiceBundle\Entity\User
     * @ORM\ManyToOne(targetEntity="\CherezWeb\ServiceBundle\Entity\User")
     * @ORM\JoinColumn(nullable=false)
     */
    private $user;

    public function setUser($user) {
        $this->user = $user;
    }

    /**
     * @return \CherezWeb\ServiceBundle\Entity\User
     */
    public function getUser() {
        return $this->user;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * @var ArrayCollection
     * @ORM\OneToMany(targetEntity="\CherezWeb\ProjectBundle\Entity\ChannelAccess", mappedBy="client", cascade={"remove"})
     */
    private $accesses;
    
    /**
     * @return ArrayCollection
     */
    public function getAccesses() {
        return $this->accesses;
    }
    
    public function getChannels() {
        $channels = [];
        foreach ($this->getAccesses() as $access) {
            /* @var $access ChannelAccess */
            $channels[$access->getChannel()->getId()] = $access->getChannel();
        }
        return $channels;
    }
}
