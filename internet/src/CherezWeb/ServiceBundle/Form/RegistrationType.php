<?php

namespace CherezWeb\ServiceBundle\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolverInterface;

class RegistrationType extends AbstractType {
    
    public function buildForm(FormBuilderInterface $builder, array $options) {
        $builder->add('email', 'email', array(
            'label' => 'Электронная почта',
            'attr' => array(
                'placeholder' => "Адрес электронной почты",
            ),
        ));
        
        $builder->add('password', 'repeated', array(
            'type' => 'password',
            'invalid_message' => 'Пароль и его подтверждение не совпадают.',
            'first_options'  => array('label' => 'Пароль', 'attr' => array(
                'placeholder' => "Пароль новой учетной записи",
            )),
            'second_options' => array('label' => 'Подтверждение пароля', 'attr' => array(
                'placeholder' => "Повторите ввод пароля, чтобы проверить себя",
            )),
        ));
        
        $builder->add('captcha', 'captcha', array(
            'label' => 'Проверочный код',
            'attr' => array(
                'class' => 'gregwar-captcha',
                'placeholder' => "Введите код с картинки",
            )
        ));
        
        $builder->add('save', 'submit', array('label' => 'Зарегистрироваться', 'attr' => array('class' => 'btn-primary registration-btn')));
    }

    public function setDefaultOptions(OptionsResolverInterface $resolver) {
        $resolver->setDefaults(array(
            'validation_groups' => array($this->getName()),
            'data_class' => 'CherezWeb\ServiceBundle\Entity\User',
            'attr' => array(
                'novalidate' => 'novalidate',
                'style' => 'display: none;'
            ),
            'csrf_message' => 'Страница долго не использовалась и форма устарела. Повторите попытку, в этот раз все должно пройти удачно.',
        ));
    }

    public function getName() {
        $reflect = new \ReflectionClass($this);
        return $reflect->getShortName();
    }

}
