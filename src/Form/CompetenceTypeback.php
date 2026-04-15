<?php

namespace App\Form;

use App\Entity\competence\Competence;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;

class CompetenceTypeback extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('category', TextType::class, [
                'required' => true,
            ])
            ->add('type', TextType::class, [
                'required' => true,
            ])
            ->add('description', TextareaType::class, [
                'required' => true,
            ])
            ->add('niveau', ChoiceType::class, [
                'choices' => [
                    'Débutant' => 'Débutant',
                    'Intermédiaire' => 'Intermédiaire',
                    'Avancé' => 'Avancé',
                    'Expert' => 'Expert',
                ],
                'required' => true,
                'placeholder' => false,
            ])
            ->add('anneesExperience', IntegerType::class, [
                'required' => true,
                'empty_data' => '0',
            ])
            ->add('certification', TextType::class, [
                'required' => false,
            ])
            ->add('statut', ChoiceType::class, [
                'choices' => [
                    'Validée' => 'Validée',
                    'En cours' => 'En cours',
                    'Expirée' => 'Expirée',
                ],
                'required' => true,
                'placeholder' => false,
            ])
            ->add('email', EmailType::class, [
                'required' => true,
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Competence::class,
        ]);
    }

    public function getBlockPrefix(): string
    {
        return 'competence_type';
    }
}