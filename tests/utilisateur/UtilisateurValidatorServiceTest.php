<?php

namespace App\Tests\Service\utilisateur;

use App\Entity\utilisateur\Utilisateur;
use App\Service\utilisateur\UtilisateurValidatorService;
use PHPUnit\Framework\TestCase;

class UtilisateurValidatorServiceTest extends TestCase
{
    private function createValidUtilisateur(): Utilisateur
    {
        $utilisateur = new Utilisateur();

        $utilisateur->setNom('Hamza');
        $utilisateur->setPrenom('Test');
        $utilisateur->setEmail('hamza@gmail.com');
        $utilisateur->setMotDePasse('12345678');
        $utilisateur->setTelephone('52122202');
        $utilisateur->setRole('freelancer');
        $utilisateur->setStatut('actif');

        return $utilisateur;
    }

    public function testUtilisateurValide(): void
    {
        $utilisateur = $this->createValidUtilisateur();

        $service = new UtilisateurValidatorService();

        $this->assertTrue($service->validate($utilisateur));
    }

    public function testNomObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $utilisateur = $this->createValidUtilisateur();
        $utilisateur->setNom('');

        $service = new UtilisateurValidatorService();
        $service->validate($utilisateur);
    }

    public function testPrenomObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $utilisateur = $this->createValidUtilisateur();
        $utilisateur->setPrenom('');

        $service = new UtilisateurValidatorService();
        $service->validate($utilisateur);
    }

    public function testEmailObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $utilisateur = $this->createValidUtilisateur();
        $utilisateur->setEmail('');

        $service = new UtilisateurValidatorService();
        $service->validate($utilisateur);
    }

    public function testEmailInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $utilisateur = $this->createValidUtilisateur();
        $utilisateur->setEmail('email_invalide');

        $service = new UtilisateurValidatorService();
        $service->validate($utilisateur);
    }

    public function testMotDePasseObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $utilisateur = $this->createValidUtilisateur();
        $utilisateur->setMotDePasse('');

        $service = new UtilisateurValidatorService();
        $service->validate($utilisateur);
    }

    public function testMotDePasseTropCourt(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $utilisateur = $this->createValidUtilisateur();
        $utilisateur->setMotDePasse('123');

        $service = new UtilisateurValidatorService();
        $service->validate($utilisateur);
    }

    public function testTelephoneInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $utilisateur = $this->createValidUtilisateur();
        $utilisateur->setTelephone('123');

        $service = new UtilisateurValidatorService();
        $service->validate($utilisateur);
    }

    public function testRoleInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $utilisateur = $this->createValidUtilisateur();
        $utilisateur->setRole('role_invalide');

        $service = new UtilisateurValidatorService();
        $service->validate($utilisateur);
    }

    public function testStatutInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $utilisateur = $this->createValidUtilisateur();
        $utilisateur->setStatut('statut_invalide');

        $service = new UtilisateurValidatorService();
        $service->validate($utilisateur);
    }
}