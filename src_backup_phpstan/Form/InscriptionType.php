<?php

namespace App\Form;

use App\Entity\utilisateur\Utilisateur;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\TelType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints as Assert;

class InscriptionType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
{
    $builder
        ->add('nom', TextType::class, [
            'constraints' => [
                new Assert\NotBlank(['message' => 'Le nom est obligatoire.']),
                new Assert\Length([
                    'min' => 2,
                    'minMessage' => 'Le nom doit contenir au moins {{ limit }} caractères.',
                    'max' => 50,
                    'maxMessage' => 'Le nom ne peut pas dépasser {{ limit }} caractères.',
                ]),
                new Assert\Regex([
                    'pattern' => '/^[a-zA-ZÀ-ÿ\s\-]+$/',
                    'message' => 'Le nom ne doit contenir que des lettres.',
                ]),
            ],
        ])
        ->add('prenom', TextType::class, [
            'constraints' => [
                new Assert\NotBlank(['message' => 'Le prénom est obligatoire.']),
                new Assert\Length([
                    'min' => 2,
                    'minMessage' => 'Le prénom doit contenir au moins {{ limit }} caractères.',
                    'max' => 50,
                    'maxMessage' => 'Le prénom ne peut pas dépasser {{ limit }} caractères.',
                ]),
                new Assert\Regex([
                    'pattern' => '/^[a-zA-ZÀ-ÿ\s\-]+$/',
                    'message' => 'Le prénom ne doit contenir que des lettres.',
                ]),
            ],
        ])
        ->add('email', EmailType::class, [
            'constraints' => [
                new Assert\NotBlank(['message' => "L'adresse email est obligatoire."]),
                new Assert\Email(['message' => "L'adresse email « {{ value }} » n'est pas valide."]),
            ],
        ])
        ->add('motDePasse', PasswordType::class, [
            'mapped' => false,
            'constraints' => [
                new Assert\NotBlank(['message' => 'Le mot de passe est obligatoire.']),
                new Assert\Length([
                    'min' => 8,
                    'minMessage' => 'Le mot de passe doit contenir au moins {{ limit }} caractères.',
                ]),
                new Assert\Regex([
                    'pattern' => '/[A-Z]/',
                    'message' => 'Le mot de passe doit contenir au moins une lettre majuscule.',
                ]),
                new Assert\Regex([
                    'pattern' => '/[a-z]/',
                    'message' => 'Le mot de passe doit contenir au moins une lettre minuscule.',
                ]),
                new Assert\Regex([
                    'pattern' => '/\d/',
                    'message' => 'Le mot de passe doit contenir au moins un chiffre.',
                ]),
                new Assert\Regex([
                    'pattern' => '/[@$!%*?&]/',
                    'message' => 'Le mot de passe doit contenir au moins un caractère spécial (@$!%*?&).',
                ]),
            ],
        ])
        ->add('confirmMotDePasse', PasswordType::class, [
            'mapped' => false,
            'constraints' => [
                new Assert\NotBlank(['message' => 'La confirmation du mot de passe est obligatoire.']),
            ],
        ])
        ->add('telephone', TelType::class, [
            'required' => true,
            'constraints' => [
                new Assert\NotBlank(['message' => 'Le numéro de téléphone est obligatoire.']),
                new Assert\Regex([
                    'pattern' => '/^\+?[0-9\s\-]{8,15}$/',
                    'message' => 'Le numéro de téléphone n\'est pas valide.',
                ]),
            ],
        ])
        ->add('role', ChoiceType::class, [
            'choices' => [
                'Freelance'     => 'freelance',
                'Entrepreneur'  => 'entrepreneur',
                'Admin'         => 'admin',
            ],
            'constraints' => [
                new Assert\NotBlank(['message' => 'Veuillez choisir un rôle.']),
            ],
        ])
        ->add('cleAcces', TextType::class, [
            'required' => false,
        ]);
}

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Utilisateur::class,
        ]);
    }
}