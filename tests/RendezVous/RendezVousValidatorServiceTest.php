<?php

namespace App\Tests\Service\RendezVous;

use App\Entity\RendezVous\RendezVous;
use App\Service\RendezVous\RendezVousValidatorService;
use PHPUnit\Framework\TestCase;

class RendezVousValidatorServiceTest extends TestCase
{
    private function createValidRendezVous(): RendezVous
    {
        $rendezVous = new RendezVous();

        $rendezVous->setIdAdminCreateur(1);
        $rendezVous->setMailUser('user@gmail.com');
        $rendezVous->setTitre('Session Symfony');
        $rendezVous->setDescription('Rendez-vous pour apprendre Symfony');
        $rendezVous->setDateRendezVous(new \DateTimeImmutable('+2 days'));
        $rendezVous->setType('En ligne');
        $rendezVous->setCompetence('Symfony');
        $rendezVous->setDateCreationRendezVous(new \DateTimeImmutable('now'));
        $rendezVous->setNombrePlaces(5);

        return $rendezVous;
    }

    public function testRendezVousValide(): void
    {
        $rendezVous = $this->createValidRendezVous();

        $service = new RendezVousValidatorService();

        $this->assertTrue($service->validate($rendezVous));
    }

    public function testAdminCreateurObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $rendezVous = $this->createValidRendezVous();
        $rendezVous->setIdAdminCreateur(null);

        $service = new RendezVousValidatorService();
        $service->validate($rendezVous);
    }

    public function testEmailInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $rendezVous = $this->createValidRendezVous();
        $rendezVous->setMailUser('email_invalide');

        $service = new RendezVousValidatorService();
        $service->validate($rendezVous);
    }

    public function testTitreObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $rendezVous = $this->createValidRendezVous();
        $rendezVous->setTitre(null);

        $service = new RendezVousValidatorService();
        $service->validate($rendezVous);
    }

    public function testDateRendezVousObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $rendezVous = $this->createValidRendezVous();
        $rendezVous->setDateRendezVous(null);

        $service = new RendezVousValidatorService();
        $service->validate($rendezVous);
    }

    public function testDateRendezVousDansLePasse(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $rendezVous = $this->createValidRendezVous();
        $rendezVous->setDateRendezVous(new \DateTimeImmutable('-1 day'));

        $service = new RendezVousValidatorService();
        $service->validate($rendezVous);
    }

    public function testTypeObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $rendezVous = $this->createValidRendezVous();
        $rendezVous->setType(null);

        $service = new RendezVousValidatorService();
        $service->validate($rendezVous);
    }

    public function testCompetenceObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $rendezVous = $this->createValidRendezVous();
        $rendezVous->setCompetence(null);

        $service = new RendezVousValidatorService();
        $service->validate($rendezVous);
    }

    public function testNombrePlacesInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $rendezVous = $this->createValidRendezVous();
        $rendezVous->setNombrePlaces(0);

        $service = new RendezVousValidatorService();
        $service->validate($rendezVous);
    }
}