<?php

namespace CherezWeb\ProjectBundle\Entity;

use Doctrine\ORM\Mapping as ORM;
use \Doctrine\Common\Collections\ArrayCollection;

/**
 * @ORM\Table(name="chwpb_channel")
 * @ORM\Entity(repositoryClass="CherezWeb\ProjectBundle\Repository\ChannelRepository")
 */
class Channel {
    
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
     * @var \CherezWeb\ProjectBundle\Entity\Server
     * @ORM\ManyToOne(targetEntity="\CherezWeb\ProjectBundle\Entity\Server", inversedBy="channels")
     * @ORM\JoinColumn(nullable=false)
     */
    private $server;

    public function setServer(Server $server) {
        $this->server = $server;
    }

    /**
     * @return \CherezWeb\ProjectBundle\Entity\Server
     */
    public function getServer() {
        return $this->server;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * @ORM\Column(type="string", length=100, unique=true)
     */
    private $uid;

    public function setUid($uid) {
        $this->uid = $uid;
    }

    public function getUid() {
        return $this->uid;
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
     * @ORM\Column(name="_order", type="integer")
     */
    private $order;

    public function setOrder($order) {
        $this->order = $order;
    }

    public function getOrder() {
        return $this->order;
    }

    //--------------------------------------------------------------------------
    
    /**
     * @ORM\Column(type="string", length=500, nullable=true)
     */
    private $path;

    /**
     * @param string $path / - separator
     */
    public function setPath($path) {
        $this->path = $path;
    }

    public function getPath() {
        return $this->path;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * @var ArrayCollection
     * @ORM\OneToMany(targetEntity="\CherezWeb\ProjectBundle\Entity\ChannelAccess", mappedBy="channel", cascade={"remove"})
     */
    private $accesses;
    
    /**
     * @return ArrayCollection
     */
    public function getAccesses() {
        return $this->accesses;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * @var ArrayCollection
     * @ORM\OneToMany(targetEntity="\CherezWeb\ProjectBundle\Entity\ChannelRequest", mappedBy="channel", cascade={"remove"})
     */
    private $requests;
    
}
