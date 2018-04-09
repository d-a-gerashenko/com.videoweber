<?php

namespace CherezWeb\BillingBundle\Entity;

use Doctrine\ORM\Mapping as ORM;

/**
 * @ORM\Table(name="chwbb_payment")
 * @ORM\Entity
 */
class Payment {
    
    public function __construct() {
        $this->created = new \DateTime();
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
    
    const PROVIDER_ROBOKASSA = 'robokassa';
    
    /**
     * @ORM\Column(type="string", columnDefinition="ENUM('robokassa')")
     */
    private $provider;

    public function setProvider($provider) {
        $this->provider = $provider;
    }

    public function getProvider() {
        return $this->provider;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * Сумма платежа в копейках, как положительная так и отрицательная.
     * @ORM\Column(type="integer")
     */
    protected $sum;

    /**
     * @param integer $sum Сумма платежа в копейках, как положительная так и отрицательная.
     */
	public function setSum($sum) {
		$this->sum = $sum;
	}

    /**
     * 
     * @return integer Сумма платежа в копейках, как положительная так и отрицательная.
     */
	public function getSum() {
		return $this->sum;
	}

    //--------------------------------------------------------------------------

    /**
     * @ORM\Column(type="string", length=500)
     */
    private $comment;

    public function setComment($comment) {
        $this->comment = $comment;
    }

    public function getComment() {
        return $this->comment;
    }

    //--------------------------------------------------------------------------

    /**
     * URL перенаправления после выполнения платежа.
     * @ORM\Column(type="string", length=1000)
     */
    private $returnURL;

    /**
     * @param string $returnURL URL перенаправления после выполнения платежа.
     */
    public function setReturnURL($returnURL) {
        $this->returnURL = $returnURL;
    }

    /**
     * @return string URL перенаправления после выполнения платежа.
     */
    public function getReturnURL() {
        return $this->returnURL;
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
     * @ORM\Column(type="utcdatetime", nullable=true)
     */
    protected $finalized;

    /**
	 * @param \DateTime $finalized utcdatetime
	 */
	public function setFinalized($finalized) {
		$this->finalized = $finalized;
	}

    /**
	 * @return \DateTime utcdatetime
	 */
	public function getFinalized() {
        if ($this->finalized === NULL){
            return null;
        }
		$this->finalized->setTimeZone(new \DateTimeZone(date_default_timezone_get()));
		return $this->finalized;
	}
    
    //--------------------------------------------------------------------------
    
    const RESULT_SUCCESS = 'success';
    const RESULT_FAIL = 'fail';
    
    /**
     * @ORM\Column(type="string", columnDefinition="ENUM('success','fail')", nullable=true)
     */
    private $result;

    public function setResult($result) {
        $this->result = $result;
    }

    public function getResult() {
        return $this->result;
    }
    
    //--------------------------------------------------------------------------

    /**
     * @ORM\OneToOne(targetEntity="Transaction", inversedBy="payment")
     */
    private $transaction;

    /**
     * @param Transaction $transaction
     */
    public function setTransaction(Transaction $transaction) {
        $this->transaction = $transaction;
    }

    /**
     * @return Transaction 
     */
    public function getTransaction() {
        return $this->transaction;
    }
    
    //--------------------------------------------------------------------------
    
    /**
	 * @ORM\ManyToOne(targetEntity="Wallet")
	 */
    private $wallet;
    
    /**
     * @param Wallet $wallet
     */
    public function setWallet(Wallet $wallet) {
        $this->wallet = $wallet;
    }

    /**
     * @return User
     */
    public function getWallet() {
        return $this->wallet;
    }

}
