<?php

namespace App\Form;

use App\Entity\RendezVous\RendezVous as RendezVousEntity;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class RendezVous extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('titre', TextType::class, [
                'label' => 'Titre',
                'attr' => ['class' => 'rv-input'],
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'required' => false,
                'attr' => [
                    'class' => 'rv-input rv-textarea',
                    'rows' => 3,
                ],
            ])
            ->add('competence', TextType::class, [
                'label' => 'Compétence',
                'attr' => ['class' => 'rv-input'],
            ])
            ->add('dateRendezVous', DateTimeType::class, [
                'label' => 'Date de rendez-vous',
                'widget' => 'single_text',
                'html5' => true,
                'attr' => ['class' => 'rv-input'],
            ])
            ->add('nombrePlaces', IntegerType::class, [
                'label' => 'Nombre de places',
                'attr' => [
                    'class' => 'rv-input',
                    'min' => 1,
                ],
            ])
            ->add('type', ChoiceType::class, [
                'label' => 'Type',
                'choices' => [
                    'Présentiel' => 'presentiel',
                    'Distance' => 'distance',
                ],
                'placeholder' => false,
                'attr' => ['class' => 'rv-input'],
            ])
            ->add('submit', SubmitType::class, [
                'label' => 'Ajouter',
                'attr' => ['class' => 'rv-btn'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => RendezVousEntity::class,
        ]);
    }
}