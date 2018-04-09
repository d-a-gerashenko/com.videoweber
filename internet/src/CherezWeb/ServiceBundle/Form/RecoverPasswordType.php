<?php

namespace CherezWeb\ServiceBundle\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolverInterface;

class RecoverPasswordType extends AbstractType {
    
    public function buildForm(FormBuilderInterface $builder, array $options) {
        
        $builder->add('password', 'repeated', array(
            'type' => 'password',
            'invalid_message' => 'Пароль и его подтверждение не совпадают.',
            'first_options'  => array('label' => 'Новый пароль', 'attr' => array('placeholder' => "Пароль новой учетной записи")),
            'second_options' => array('label' => 'Подтверждение нового пароля', 'attr' => array('placeholder' => "Повторите ввод пароля, чтобы проверить себя")),
        ));
        
        $builder->add('save', 'submit', array('label' => 'Сохранить новый пароль', 'attr' => array('class' => 'btn-primary')));
    }

    public function setDefaultOptions(OptionsResolverInterface $resolver) {
        $resolver->setDefaults(array(
            'validation_groups' => array($this->getName(), "Default"),
            'data_class' => 'CherezWeb\ServiceBundle\Entity\User',
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
