<?php

namespace CherezWeb\ServiceBundle\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Security\Core\User\AdvancedUserInterface;
use Symfony\Component\Security\Core\User\EquatableInterface;
use Symfony\Component\Security\Core\User\UserInterface;

/**
 * @ORM\Table(name="chwh_user")
 * @ORM\Entity(repositoryClass="CherezWeb\ServiceBundle\Repository\UserRepository")
 */
class User implements AdvancedUserInterface, \Serializable, EquatableInterface {

    public function __construct() {
        $this->isVerified = false;
        $this->salt = md5(uniqid(null, true));
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
     * @ORM\Column(type="string", length=64, nullable=true)
     */
    private $password;

    public function setPassword($password) {
        $this->password = $password;
    }

    public function getPassword() {
        return $this->password;
    }
    
    static function generatePassword($length = 10) {
        return \CherezWeb\Lib\Common\RandomStringGenerator::generate($length);
    }

    //--------------------------------------------------------------------------

    /**
     * @ORM\Column(type="string", length=100, unique=true)
     */
    private $email;

    public function setEmail($email) {
        $this->email = $email;
    }

    public function getEmail() {
        return $this->email;
    }

    /**
     * Возвращает email
     * @return string
     */
    public function getUsername() {
        return $this->email;
    }

    //--------------------------------------------------------------------------

    /**
     * @ORM\Column(type="boolean")
     */
    private $isVerified;

    public function setIsVerified($isVerified) {
        $this->isVerified = $isVerified;
    }

    public function getIsVerified() {
        return $this->isVerified;
    }

    //--------------------------------------------------------------------------

    /**
     * @ORM\Column(type="string", length=32)
     */
    private $salt;

    public function setSalt($salt) {
        $this->salt = $salt;
    }

    public function getSalt() {
        return $this->salt;
    }

    //--------------------------------------------------------------------------
    const ROLE_ADMIN = 'ROLE_ADMIN';
    const ROLE_USER = 'ROLE_USER';
    
    private $roles = null;
    
    public function setRoles($roles = array())
	{
		$roles[] = self::ROLE_USER;
		
		$this->roles = array_unique($roles);
	
		return $this->roles;
	}

    public function getRoles() {
        if ($this->roles === null) {
			
            $this->roles = array();
            
			$this->roles[] = self::ROLE_USER;
		}
		
		return $this->roles;
    }

    //--------------------------------------------------------------------------

    public function eraseCredentials() {}

    //--------------------------------------------------------------------------

    /**
     * @see \Serializable::serialize()
     */
    public function serialize() {
        return serialize(array(
            $this->id,
            $this->email,
            $this->password,
            $this->salt,
        ));
    }

    /**
     * @see \Serializable::unserialize()
     */
    public function unserialize($serialized) {
        list (
            $this->id,
            $this->email,
            $this->password,
            $this->salt,
        ) = unserialize($serialized);
    }
    
    //--------------------------------------------------------------------------

    public function isEqualTo(UserInterface $user) {
        if (
                !$user instanceof User
            ||
                $user->getPassword() !== $this->getPassword()
            ||
                $user->getUsername() !== $this->getUsername()
            ||
                $user->getSalt() !== $this->getSalt()
        ) {
            return FALSE;
        }
        return TRUE;
	}
    
    //--------------------------------------------------------------------------
    
    public function isAccountNonExpired() {
        return TRUE;
    }
    
    public function isAccountNonLocked() {
        return TRUE;
    }
    
    public function isCredentialsNonExpired() {
        return TRUE;
    }
    
    public function isEnabled() {
        return $this->getIsVerified();
    }
    
    //--------------------------------------------------------------------------

    /**
     * @var \CherezWeb\BillingBundle\Entity\Wallet
     * @ORM\OneToOne(targetEntity="\CherezWeb\BillingBundle\Entity\Wallet", cascade={"persist"})
     */
    private $wallet;

    public function setWallet($wallet) {
        $this->wallet = $wallet;
    }

    /**
     * @return \CherezWeb\BillingBundle\Entity\Wallet
     */
    public function getWallet() {
        if ($this->wallet === NULL) {
            $this->wallet = new \CherezWeb\BillingBundle\Entity\Wallet;
        }
        return $this->wallet;
    }
    
}