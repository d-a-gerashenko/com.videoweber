<?php

namespace CherezWeb\ProjectBundle\Entity;

use Doctrine\ORM\Mapping as ORM;

/**
 * @ORM\Table(name="chwpb_channel_response")
 * @ORM\Entity(repositoryClass="CherezWeb\ProjectBundle\Repository\ChannelResponseRepository")
 */
class ChannelResponse {
    
     public function __construct()
     {
         $this->setUploaded(new \DateTime);
         $this->size = 0;
     }
     
     //-------------------------------------------------------------------------
    
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
     * @var \CherezWeb\ProjectBundle\Entity\ChannelRequest
     * @ORM\OneToOne(targetEntity="\CherezWeb\ProjectBundle\Entity\ChannelRequest", inversedBy="response")
     * @ORM\JoinColumn(name="request_id", referencedColumnName="id", onDelete="SET NULL")
     */
    private $request;

    public function setRequest(ChannelRequest $request) {
        $this->request = $request;
    }

    /**
     * @return \CherezWeb\ProjectBundle\Entity\ChannelRequest
     */
    public function getRequest() {
        return $this->request;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * utcdatetime
     * @var \DateTime
     * @ORM\Column(type="utcdatetime")
     */
    protected $uploaded;

	public function setUploaded(\DateTime $uploaded) {
		$this->uploaded = $uploaded;
	}

    /**
	 * @return \DateTime utcdatetime
	 */
	public function getUploaded() {
        if ($this->uploaded !== NULL) {
            $this->uploaded->setTimeZone(new \DateTimeZone(date_default_timezone_get()));
        }
		return $this->uploaded;
	}
    
    //--------------------------------------------------------------------------
    
    /**
     * Size in bytes.
     * @ORM\Column(type="integer")
     */
    private $size;
    
    /**
     * @param integer $size Data size in bytes.
     */
    function setSize($size)
    {
        $this->size = $size;
    }
    /**
     * Data size in bytes.
     *
     * @return integer 
     */
    public function getSize() {
        return $this->size;
    }
    
}
