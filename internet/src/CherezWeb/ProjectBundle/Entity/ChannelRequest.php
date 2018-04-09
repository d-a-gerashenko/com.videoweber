<?php

namespace CherezWeb\ProjectBundle\Entity;

use Doctrine\ORM\Mapping as ORM;

/**
 * @ORM\Table(name="chwpb_channel_request")
 * @ORM\Entity(repositoryClass="CherezWeb\ProjectBundle\Repository\ChannelRequestRepository")
 */
class ChannelRequest {
    
     public function __construct()
     {
        $this->setUpdated(new \DateTime);
        $this->setCreated(new \DateTime);
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
     * @var \CherezWeb\ProjectBundle\Entity\Channel
     * @ORM\ManyToOne(targetEntity="\CherezWeb\ProjectBundle\Entity\Channel", inversedBy="requests")
     * @ORM\JoinColumn(nullable=false)
     */
    private $channel;

    public function setChannel(Channel $channel) {
        $this->channel = $channel;
    }

    /**
     * @return \CherezWeb\ProjectBundle\Entity\Channel
     */
    public function getChannel() {
        return $this->channel;
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
     * @ORM\Column(type="text")
     */
    private $data;

    public function setData($data) {
        $this->data = $data;
    }

    public function getData() {
        return $this->data;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * utcdatetime
     * @var \DateTime
     * @ORM\Column(type="utcdatetime")
     */
    protected $created;

	public function setCreated(\DateTime $created) {
		$this->created = $created;
	}

    /**
	 * @return \DateTime utcdatetime
	 */
	public function getCreated() {
        if ($this->created !== NULL) {
            $this->created->setTimeZone(new \DateTimeZone(date_default_timezone_get()));
        }
		return $this->created;
	}
    
    //--------------------------------------------------------------------------
    
    /**
     * utcdatetime
     * @var \DateTime
     * @ORM\Column(type="utcdatetime")
     */
    protected $updated;

	public function setUpdated(\DateTime $updated) {
		$this->updated = $updated;
	}

    /**
	 * @return \DateTime utcdatetime
	 */
	public function getUpdated() {
        if ($this->updated !== NULL) {
            $this->updated->setTimeZone(new \DateTimeZone(date_default_timezone_get()));
        }
		return $this->updated;
	}
    
    //--------------------------------------------------------------------------

    /**
     * @var \CherezWeb\ProjectBundle\Entity\Client
     * @ORM\ManyToOne(targetEntity="\CherezWeb\ProjectBundle\Entity\Client")
     * @ORM\JoinColumn(nullable=false)
     */
    private $client;

    public function setClient(Client $client) {
        $this->client = $client;
    }

    /**
     * @return \CherezWeb\ProjectBundle\Entity\Client
     */
    public function getClient() {
        return $this->client;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * @ORM\OneToOne(targetEntity="\CherezWeb\ProjectBundle\Entity\ChannelResponse", mappedBy="request", cascade={"remove"})
     */
    private $response;
    
    public function setResponse(ChannelResponse $response)
    {
        $this->response = $response;
    }
    
    /**
     * @return ChannelResponse
     */
    public function getResponse() {
        return $this->response;
    }
    
}
