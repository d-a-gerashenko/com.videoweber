<?php

namespace CherezWeb\ProjectBundle\Entity;

use Doctrine\ORM\Mapping as ORM;
use Doctrine\Common\Collections\ArrayCollection;

/**
 * @ORM\Table(name="chwpb_server")
 * @ORM\Entity(repositoryClass="CherezWeb\ProjectBundle\Repository\ServerRepository")
 */
class Server implements RemoteAppInterface {
    
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
     * @ORM\Column(name="_version", type="string", length=100, nullable=true)
     */
    private $version;

    public function setVersion($version) {
        $this->version = $version;
    }

    public function getVersion() {
        return $this->version;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * @ORM\Column(type="string", length=100)
     */
    private $location;

    public function setLocation($location) {
        $this->location = $location;
    }

    public function getLocation() {
        return $this->location;
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

    public function setUser(\CherezWeb\ServiceBundle\Entity\User $user) {
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
     * @ORM\OneToMany(targetEntity="\CherezWeb\ProjectBundle\Entity\Channel", mappedBy="server", cascade={"remove"})
     * @ORM\OrderBy({"title" = "ASC"})
     */
    private $channels;
    
    /**
     * @return ArrayCollection
     */
    public function getChannels() {
        return $this->channels;
    }
    
}
