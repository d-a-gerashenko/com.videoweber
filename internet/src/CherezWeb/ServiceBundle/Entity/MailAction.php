<?php

namespace CherezWeb\ServiceBundle\Entity;

use Doctrine\ORM\Mapping as ORM;

/**
 * @ORM\Table(name="chwh_mail_action")
 * @ORM\Entity(repositoryClass="CherezWeb\ServiceBundle\Repository\MailActionRepository")
 */
class MailAction {
    
    public function __construct() {
        $this->created = new \DateTime();
        $this->code = md5(uniqid(null, true));
        $this->parameters = array();
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * @ORM\Column(type="integer")
     * @ORM\Id
     * @ORM\GeneratedValue(strategy="AUTO")
     */
    private $id;

    /**
     * @return integer 
     */
    public function getId() {
        return $this->id;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * @var User
     * @ORM\ManyToOne(targetEntity="User")
     * @ORM\JoinColumn(nullable=false)
     */
    private $user;
    
    /**
     * @return User
     */
    public function getUser() {
        return $this->user;
    }

    public function setUser(User $user) {
        $this->user = $user;
    }
    
    //--------------------------------------------------------------------------

    /**
     * @ORM\Column(type="string", length=100)
     */
    private $email;

    public function setEmail($email) {
        $this->email = $email;
    }

    public function getEmail() {
        return $this->email;
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
     * @ORM\Column(type="string", length=32, unique=true)
     */
    private $code;

    public function setCode($code) {
        $this->code = $code;
    }

    public function getCode() {
        return $this->code;
    }
    
    //--------------------------------------------------------------------------

    const TYPE_VERIFY = 'verify';
    const TYPE_RECOVER = 'recover';
    const TYPE_CHANGE_MAIL = 'change_mail';

    /**
     * @ORM\Column(type="string", columnDefinition="ENUM('verify', 'recover', 'change_mail')")
     */
    protected $type;

    public function getType() {
        return $this->type;
    }

    public function setType($type) {
        if (!in_array($type, array(
            self::TYPE_VERIFY,
            self::TYPE_RECOVER,
            self::TYPE_CHANGE_MAIL,
        ))) {
            throw new \Exception(sprintf('Неправильный формат $type: %s.', $type));
        }
        $this->type = $type;
    }
    
}
