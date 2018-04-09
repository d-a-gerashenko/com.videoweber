<?php

namespace CherezWeb\BillingBundle\Entity;

use Doctrine\ORM\Mapping as ORM;

/**
 * @ORM\Table(name="chwbb_transaction")
 * @ORM\Entity(repositoryClass="CherezWeb\BillingBundle\Repository\TransactionRepository")
 */
class Transaction {

    public function __construct() {
        $this->created = new \DateTime();
        $this->sum = 0;
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
     * Сумма транзакции в копейках, как положительная так и отрицательная.
     * @ORM\Column(type="integer")
     */
    protected $sum;

    /**
     * @param integer $sum Сумма транзакции в копейках, как положительная так и отрицательная.
     */
	public function setSum($sum) {
		$this->sum = $sum;
	}

    /**
     * 
     * @return integer Сумма транзакции в копейках, как положительная так и отрицательная.
     */
	public function getSum() {
		return $this->sum;
	}
    
    //--------------------------------------------------------------------------
    
    /**
     * Сумма остатка в копейках, после операции.
     * @ORM\Column(type="integer")
     */
    protected $balanceAfter;

    /**
     * @param integer $balanceAfter Сумма остатка в копейках, после операции.
     */
	public function setBalanceAfter($balanceAfter) {
		$this->balanceAfter = $balanceAfter;
	}

    /**
     * 
     * @return integer Сумма остатка в копейках, после операции.
     */
	public function getBalanceAfter() {
		return $this->balanceAfter;
	}
    
    //--------------------------------------------------------------------------
    
    /**
	 * @ORM\ManyToOne(targetEntity="Wallet")
     * @ORM\JoinColumn(nullable=false)
	 */
    private $wallet;
    
    public function setWallet(Wallet $wallet) {
        $this->wallet = $wallet;
    }

    /**
     * @return Wallet 
     */
    public function getWallet() {
        return $this->wallet;
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
     * Payment ссылается на Transaction потому метод set не нужен.
     * @var Payment
     * @ORM\OneToOne(targetEntity="Payment", mappedBy="transaction")
     */
    private $payment;

    /**
     * @return Payment
     */
    public function getPayment() {
        return $this->payment;
    }

}