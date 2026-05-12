<?php

namespace App\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\CheckboxType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\Email;
use Symfony\Component\Validator\Constraints\IsTrue;
use Symfony\Component\Validator\Constraints\NotBlank;

class LoginType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            // ── Champ Email ──────────────────────────────────────────────
            ->add('email', EmailType::class, [
                'label'    => false,
                'attr'     => [
                    'class'        => 'input',
                    'placeholder'  => 'Email',
                    'autocomplete' => 'email',
                    'list'         => 'email-suggestions',
                ],
                'constraints' => [
                    new NotBlank(['message' => 'Veuillez entrer votre adresse e-mail.']),
                    new Email(['message'   => 'Adresse e-mail invalide.']),
                ],
            ])

            // ── Champ Mot de passe ───────────────────────────────────────
            ->add('password', PasswordType::class, [
                'label' => false,
                'attr'  => [
                    'class'        => 'input',
                    'placeholder'  => 'Mot de passe',
                    'autocomplete' => 'current-password',
                    'id'           => 'password',
                ],
                'constraints' => [
                    new NotBlank(['message' => 'Veuillez entrer votre mot de passe.']),
                ],
            ])

            // ── Anti-robot checkbox ──────────────────────────────────────
            ->add('not_robot', CheckboxType::class, [
                'label'       => 'Je ne suis pas un robot',
                'mapped'      => false,   // pas lié à une entité
                'required'    => true,
                'attr'        => ['class' => 'checkbox-input'],
                'label_attr'  => ['class' => 'checkbox'],
                'constraints' => [
                    new IsTrue(['message' => 'Veuillez confirmer que vous n\'êtes pas un robot.']),
                ],
            ])

            // ── Bouton Se connecter ──────────────────────────────────────
            ->add('submit', SubmitType::class, [
                'label' => 'Se connecter',
                'attr'  => ['class' => 'btn'],
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            // Le formulaire de login Symfony gère lui-même le CSRF via security.yaml
            // On désactive le CSRF du composant Form pour éviter le double token
            'csrf_protection' => false,
            'data_class'      => null,
        ]);
    }

    /**
     * Nom du bloc utilisé par Symfony pour identifier le formulaire.
     * Laisser vide permet à Symfony de générer le token CSRF via authenticate.
     */
    public function getBlockPrefix(): string
    {
        return '';
    }
}