<?php

namespace CherezWeb\ServiceBundle\Repository;

use Doctrine\ORM\EntityRepository;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Security\Core\User\UserProviderInterface;
use Symfony\Component\Security\Core\Exception\UsernameNotFoundException;
use Symfony\Component\Security\Core\Exception\UnsupportedUserException;
use Doctrine\ORM\NoResultException;

use CherezWeb\ServiceBundle\Entity\User;
use CherezWeb\BillingBundle\Entity\Wallet;

class UserRepository extends EntityRepository implements UserProviderInterface {

    public function loadUserByUsername($username) {
        $q = $this
                ->createQueryBuilder('u')
                ->where('u.email = :email')
                ->setParameter('email', $username)
                ->getQuery();

        try {
            // The Query::getSingleResult() method throws an exception
            // if there is no record matching the criteria.
            $user = $q->getSingleResult();
        } catch (NoResultException $e) {
            $message = sprintf(
                    'Unable to find user object identified by "%s".', $username
            );
            throw new UsernameNotFoundException($message, 0, $e);
        }
        
        $this->loadRoles($user);
        return $user;
    }

    public function refreshUser(UserInterface $user) {
        $class = get_class($user);
        if (!$this->supportsClass($class)) {
            throw new UnsupportedUserException(
                sprintf(
                    'Instances of "%s" are not supported.', $class
                )
            );
        }

        $refreshedUser = $this->find($user->getId());
        try {
            if ($refreshedUser === null) {
                throw new NoResultException(
                    sprintf(
                        'Unable to find user object identified by "%s".', $user->getId()
                    )
                );
            }
            if (!$refreshedUser->isEqualTo($user)) {
                throw new NoResultException('User data is out of date.');
            }
        } catch (NoResultException $e) {
            throw new UsernameNotFoundException(sprintf('Unable to find a user object'), 0, $e);
        }
        
        $this->loadRoles($refreshedUser);
        return $refreshedUser;
    }
    
    public function loadRoles(User $user) {
        if ($user->getId() === null) {
            return;
        }
    }

    public function supportsClass($class) {
        return $this->getEntityName() === $class || is_subclass_of($class, $this->getEntityName());
    }
    
    public function getFlushedWallet(User $user) {
        $wallet = $user->getWallet();
        /* @var $wallet Wallet */
        if ($wallet->getId() === NULL) {
            $this->getEntityManager()->persist($wallet);
            $this->getEntityManager()->flush(array($user, $wallet));
        }
        return $wallet;
    }

}
