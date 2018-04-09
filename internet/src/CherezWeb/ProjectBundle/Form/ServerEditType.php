<?php

namespace CherezWeb\ProjectBundle\Form;

use \Symfony\Component\Form\AbstractType;
use \Symfony\Component\Form\FormBuilderInterface;
use \Symfony\Component\OptionsResolver\OptionsResolverInterface;

class ServerEditType extends AbstractType {
    public function buildForm(FormBuilderInterface $builder, array $options) {
        $builder->add('location', 'text', array(
            'label' => 'Размещение',
            'attr' => array(
                'placeholder' => "Например: Страна \\ Город \\ Адрес \\ Сервер №1",
            ),
        ));
        $builder->add('save', 'submit', array('label' => 'Сохранить'));
    }

    public function setDefaultOptions(OptionsResolverInterface $resolver) {
        $resolver->setDefaults(array(
            'validation_groups' => array($this->getName(), "Default"),
            'data_class' => 'CherezWeb\ProjectBundle\Entity\Server',
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
