<?php

namespace App\Tests\Service\RendezVous;

use App\Entity\RendezVous\Postulation;
use App\Service\RendezVous\PostulationValidatorService;
use PHPUnit\Framework\TestCase;

class PostulationValidatorServiceTest extends TestCase
{
    private function createValidPostulation(): Postulation
    {
        $postulation = new Postulation();

        $postulation->setIdRendezVous(1);
        $postulation->setIdPostulant(2);
        $postulation->setMailUser('candidat@gmail.com');
        $postulation->setMessage('Je suis intéressé par ce rendez-vous.');
        $postulation->setCv('cv_candidat.pdf');
        $postulation->setStatus('en_attente');
        $postulation->setDatePostulation(new \DateTimeImmutable('now'));
        $postulation->setRole('Freelancer');
        $postulation->setNom('Hamza');
        $postulation->setPrenom('Test');
        $postulation->setTitre('Session Symfony');

        return $postulation;
    }

    public function testPostulationValide(): void
    {
        $postulation = $this->createValidPostulation();

        $service = new PostulationValidatorService();

        $this->assertTrue($service->validate($postulation));
    }

    public function testIdRendezVousObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $postulation = $this->createValidPostulation();
        $postulation->setIdRendezVous(null);

        $service = new PostulationValidatorService();
        $service->validate($postulation);
    }

    public function testIdPostulantObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $postulation = $this->createValidPostulation();
        $postulation->setIdPostulant(null);

        $service = new PostulationValidatorService();
        $service->validate($postulation);
    }

    public function testEmailInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $postulation = $this->createValidPostulation();
        $postulation->setMailUser('email_invalide');

        $service = new PostulationValidatorService();
        $service->validate($postulation);
    }

    public function testStatusObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $postulation = $this->createValidPostulation();
        $postulation->setStatus(null);

        $service = new PostulationValidatorService();
        $service->validate($postulation);
    }

    public function testStatusInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $postulation = $this->createValidPostulation();
        $postulation->setStatus('status_invalide');

        $service = new PostulationValidatorService();
        $service->validate($postulation);
    }

    public function testCvDoitEtrePdf(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $postulation = $this->createValidPostulation();
        $postulation->setCv('cv_candidat.png');

        $service = new PostulationValidatorService();
        $service->validate($postulation);
    }
}