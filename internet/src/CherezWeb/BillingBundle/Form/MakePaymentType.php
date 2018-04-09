<?php

namespace CherezWeb\BillingBundle\Form;

use \Symfony\Component\Form\AbstractType;
use \Symfony\Component\Form\FormBuilderInterface;
use \Symfony\Component\OptionsResolver\OptionsResolverInterface;

class MakePaymentType extends AbstractType {
    public function buildForm(FormBuilderInterface $builder, array $options) {
        $builder->add('sum', 'integer', array(
            'label' => 'Сумма, р.',
            'attr' => array(
                'placeholder' => "Укажите сумму платежа",
            ),
        ));
        $builder->add('save', 'submit', array('label' => 'Перейти к оплате'));
    }

    public function setDefaultOptions(OptionsResolverInterface $resolver) {
        $resolver->setDefaults(array(
            'validation_groups' => array($this->getName(), "Default"),
            'data_class' => 'CherezWeb\BillingBundle\Entity\Payment',
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
