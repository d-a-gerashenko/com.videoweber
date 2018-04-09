<?php

namespace CherezWeb\ProjectBundle\Form;

use \Symfony\Component\Form\AbstractType;
use \Symfony\Component\Form\FormBuilderInterface;
use \Symfony\Component\OptionsResolver\OptionsResolverInterface;

class ChannelEditType extends AbstractType {
    public function buildForm(FormBuilderInterface $builder, array $options) {
        $builder->add($builder->create('path', 'text', array(
            'label' => 'Группа',
            'attr' => array(
                'placeholder' => "Например: Страна/Город/Улица/Главный вход",
            ),
        ))->addModelTransformer(new DataTransformer\GroupPathTransformer()));
        $builder->add('save', 'submit', array('label' => 'Сохранить'));
    }

    public function setDefaultOptions(OptionsResolverInterface $resolver) {
        $resolver->setDefaults(array(
            'validation_groups' => array($this->getName(), "Default"),
            'data_class' => 'CherezWeb\ProjectBundle\Entity\Channel',
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
