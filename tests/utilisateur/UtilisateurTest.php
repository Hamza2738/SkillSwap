<?php

namespace App\Tests\Entity\utilisateur;

use App\Entity\utilisateur\Utilisateur;
use PHPUnit\Framework\TestCase;

class UtilisateurTest extends TestCase
{
    public function testUtilisateurEntity(): void
    {
        $date = new \DateTimeImmutable('2026-05-10 13:00:00');

        $utilisateur = new Utilisateur();

        $utilisateur->setNom('Hamza');
        $utilisateur->setPrenom('Test');
        $utilisateur->setEmail('hamza@gmail.com');
        $utilisateur->setMotDePasse('12345678');
        $utilisateur->setTelephone('52122202');
        $utilisateur->setPhotoProfil('profil.png');
        $utilisateur->setCleAcces('ABC123');
        $utilisateur->setBio('Bio utilisateur');
        $utilisateur->setPhotoCouverture('cover.png');
        $utilisateur->setLieu('Tunis');
        $utilisateur->setRole('freelancer');
        $utilisateur->setStatut('actif');
        $utilisateur->setDateInscription($date);

        $this->assertNull($utilisateur->getIdUtilisateur());
        $this->assertSame('Hamza', $utilisateur->getNom());
        $this->assertSame('Test', $utilisateur->getPrenom());
        $this->assertSame('hamza@gmail.com', $utilisateur->getEmail());
        $this->assertSame('12345678', $utilisateur->getMotDePasse());
        $this->assertSame('52122202', $utilisateur->getTelephone());
        $this->assertSame('profil.png', $utilisateur->getPhotoProfil());
        $this->assertSame('ABC123', $utilisateur->getCleAcces());
        $this->assertSame('Bio utilisateur', $utilisateur->getBio());
        $this->assertSame('cover.png', $utilisateur->getPhotoCouverture());
        $this->assertSame('Tunis', $utilisateur->getLieu());
        $this->assertSame('freelancer', $utilisateur->getRole());
        $this->assertSame('actif', $utilisateur->getStatut());
        $this->assertSame($date, $utilisateur->getDateInscription());
    }

    public function testUtilisateurDefaultIdIsNull(): void
    {
        $utilisateur = new Utilisateur();

        $this->assertNull($utilisateur->getIdUtilisateur());
    }
}