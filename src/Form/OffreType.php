<?php

namespace App\Form;

use App\Entity\Offres\Offre;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\MoneyType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class OffreType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('titre', TextType::class)
            ->add('description', TextareaType::class, ['required' => false])
            ->add('typeOffre', ChoiceType::class, [
                'choices' => [
                    'emploi' => 'emploi',
                    'projet' => 'projet',
                    'tache' => 'tache',
                ],
            ])
            ->add('budget', MoneyType::class, [
                'required' => false,
                'currency' => 'TND',
            ])
            ->add('prix', MoneyType::class, [
                'required' => false,
                'currency' => 'TND',
            ])
            ->add('duree', IntegerType::class, ['required' => false])
            ->add('localisation', TextType::class, ['required' => false])
            ->add('dateLimite', DateType::class, [
                'required' => false,
                'widget' => 'single_text',
            ])
            ->add('statut', ChoiceType::class, [
                'choices' => [
                    'ouverte' => 'ouverte',
                    'fermee' => 'fermee',
                ],
            ])
            ->add('tags', TextType::class, ['required' => false]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Offre::class,
        ]);
    }
}