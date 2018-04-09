<?php

namespace CherezWeb\ServiceBundle\Form;

use \Symfony\Component\Form\AbstractType;
use \Symfony\Component\Form\FormBuilderInterface;
use \Symfony\Component\OptionsResolver\OptionsResolverInterface;

class ChangeEmailType extends AbstractType {
    
    public function buildForm(FormBuilderInterface $builder, array $options) {
        $builder->add('email', 'email', array(
            'label' => 'Адрес электронной почты',
            'attr' => array(
                'placeholder' => "Новый адрес электронной почты",
            ),
        ));
        
        $builder->add('password', 'password', array(
            'label' => 'Пароль',
            'attr' => array(
                'placeholder' => "Текущий пароль",
            ),
        ));
        
        $builder->add('save', 'submit', array('label' => 'Отправить ссылку изменения адреса электронной почты', 'attr' => array('class' => 'btn-primary')));
    }

    public function setDefaultOptions(OptionsResolverInterface $resolver) {
        $resolver->setDefaults(array(
            'validation_groups' => array($this->getName(), "Default"),
            'data_class' => 'CherezWeb\ServiceBundle\Entity\User',
            'attr' => array(
                'novalidate' => 'novalidate',
                'class' => 'chw-ajax-manager-form',
            ),
            'csrf_message' => 'Страница долго не использовалась и форма устарела. Повторите попытку, в этот раз все должно пройти удачно.',
        ));
    }

    public function getName() {
        $reflect = new \ReflectionClass($this);
        return $reflect->getShortName();
    }

}
