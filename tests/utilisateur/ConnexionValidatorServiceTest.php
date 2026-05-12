<?php

namespace App\Tests\Service\utilisateur;

use App\Entity\utilisateur\Connexion;
use App\Entity\utilisateur\Utilisateur;
use App\Service\utilisateur\ConnexionValidatorService;
use PHPUnit\Framework\TestCase;

class ConnexionValidatorServiceTest extends TestCase
{
    private function createValidConnexion(): Connexion
    {
        $demandeur = new Utilisateur();
        $receveur = new Utilisateur();

        $connexion = new Connexion();

        $connexion->setDemandeur($demandeur);
        $connexion->setReceveur($receveur);
        $connexion->setStatut('en_attente');
        $connexion->setDateDemande(new \DateTimeImmutable('-1 hour'));

        return $connexion;
    }

    public function testConnexionValide(): void
    {
        $connexion = $this->createValidConnexion();

        $service = new ConnexionValidatorService();

        $this->assertTrue($service->validate($connexion));
    }

    public function testDemandeurObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $connexion = $this->createValidConnexion();
        $connexion->setDemandeur(null);

        $service = new ConnexionValidatorService();
        $service->validate($connexion);
    }

    public function testReceveurObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $connexion = $this->createValidConnexion();
        $connexion->setReceveur(null);

        $service = new ConnexionValidatorService();
        $service->validate($connexion);
    }

    public function testDemandeurEtReceveurIdentiques(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $utilisateur = new Utilisateur();

        $connexion = new Connexion();
        $connexion->setDemandeur($utilisateur);
        $connexion->setReceveur($utilisateur);
        $connexion->setStatut('en_attente');
        $connexion->setDateDemande(new \DateTimeImmutable('-1 hour'));

        $service = new ConnexionValidatorService();
        $service->validate($connexion);
    }

    public function testStatutInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $connexion = $this->createValidConnexion();
        $connexion->setStatut('statut_invalide');

        $service = new ConnexionValidatorService();
        $service->validate($connexion);
    }

    public function testDateDemandeObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $connexion = $this->createValidConnexion();
        $connexion->setDateDemande(null);

        $service = new ConnexionValidatorService();
        $service->validate($connexion);
    }

    public function testDateDemandeDansLeFutur(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $connexion = $this->createValidConnexion();
        $connexion->setDateDemande(new \DateTimeImmutable('+1 day'));

        $service = new ConnexionValidatorService();
        $service->validate($connexion);
    }
}