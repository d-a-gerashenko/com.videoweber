<?php

namespace CherezWeb\ProjectBundle\Entity;

use Doctrine\ORM\Mapping as ORM;

/**
 * @ORM\Table(name="chwpb_channel_access")
 * @ORM\Entity(repositoryClass="CherezWeb\ProjectBundle\Repository\ChannelAccessRepository")
 */
class ChannelAccess {
    
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
     * @ORM\ManyToOne(targetEntity="\CherezWeb\ProjectBundle\Entity\Channel", inversedBy="accesses")
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
     * @var \CherezWeb\ProjectBundle\Entity\Client
     * @ORM\ManyToOne(targetEntity="\CherezWeb\ProjectBundle\Entity\Client", inversedBy="accesses")
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
    
}
