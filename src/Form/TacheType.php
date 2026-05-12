<?php

namespace App\Form;

use App\Entity\suiviTache\Tache;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class TacheType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('titre', TextType::class)
            ->add('description', TextareaType::class, [
                'required' => false,
            ])
            ->add('statut', ChoiceType::class, [
                'choices' => [
                    'À faire' => 'À faire',
                    'En cours' => 'En cours',
                    'Terminée' => 'Terminée',
                ],
            ])
            ->add('priorite', ChoiceType::class, [
                'choices' => [
                    'Basse' => 'Basse',
                    'Moyenne' => 'Moyenne',
                    'Haute' => 'Haute',
                ],
            ])
            ->add('evaluation', ChoiceType::class, [
                'required' => false,
                'choices' => [
                    'Excellent' => 'Excellent',
                    'Bravo' => 'Bravo',
                    'Bien' => 'Bien',
                    'Moyen' => 'Moyen',
                    'Faible' => 'Faible',
                ],
            ])
            ->add('echeance', DateType::class, [
                'widget' => 'single_text',
                'required' => false,
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Tache::class,
        ]);
    }
}