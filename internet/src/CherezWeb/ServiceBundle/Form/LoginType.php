<?php

namespace CherezWeb\ServiceBundle\Form;

use \Symfony\Component\Form\AbstractType;
use \Symfony\Component\Form\FormBuilderInterface;
use \Symfony\Component\OptionsResolver\OptionsResolverInterface;

class LoginType extends AbstractType {
    
    public function buildForm(FormBuilderInterface $builder, array $options) {
        $builder->add('email', 'email', array(
            'label' => 'Электронная почта',
            'attr' => array(
                'placeholder' => "Адрес электронной почты",
            ),
        ));
        
        $builder->add('password', 'password', array(
            'label' => 'Пароль',
            'attr' => array(
                'placeholder' => "Пароль учетной записи",
            ),
        ));
        $builder->add('save', 'submit', array('label' => 'Войти', 'attr' => array('class' => 'btn-primary')));
    }

    public function setDefaultOptions(OptionsResolverInterface $resolver) {
        $resolver->setDefaults(array(
            'validation_groups' => array($this->getName()),
            'attr' => array(
                'novalidate' => 'novalidate',
            ),
            'csrf_message' => 'Страница долго не использовалась и форма устарела. Повторите попытку, в этот раз все должно пройти удачно.',
        ));
    }

    public function getName() {
        $reflect = new \ReflectionClass($this);
        return $reflect->getShortName();
    }

}
