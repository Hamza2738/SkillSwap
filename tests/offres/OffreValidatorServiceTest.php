<?php

namespace App\Tests\Service\offres;

use App\Entity\Offres\Offre;
use App\Service\offres\OffreValidatorService;
use PHPUnit\Framework\TestCase;

class OffreValidatorServiceTest extends TestCase
{
    public function testOffreValide(): void
    {
        $offre = new Offre();

        $offre->setTitre('Développeur Symfony');
        $offre->setPrix('150');
        $offre->setBudget('500');
        $offre->setDuree(30);
        $offre->setDateLimite(new \DateTimeImmutable('+7 days'));
        $offre->setMailUser('test@gmail.com');

        $service = new OffreValidatorService();

        $this->assertTrue($service->validate($offre));
    }

    public function testTitreObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $offre = new Offre();

        $offre->setPrix('150');
        $offre->setBudget('500');
        $offre->setDuree(30);
        $offre->setDateLimite(new \DateTimeImmutable('+7 days'));
        $offre->setMailUser('test@gmail.com');

        $service = new OffreValidatorService();
        $service->validate($offre);
    }

    public function testPrixInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $offre = new Offre();

        $offre->setTitre('Offre Test');
        $offre->setPrix('0');
        $offre->setBudget('500');
        $offre->setDuree(30);
        $offre->setDateLimite(new \DateTimeImmutable('+7 days'));
        $offre->setMailUser('test@gmail.com');

        $service = new OffreValidatorService();
        $service->validate($offre);
    }

    public function testBudgetInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $offre = new Offre();

        $offre->setTitre('Offre Test');
        $offre->setPrix('150');
        $offre->setBudget('0');
        $offre->setDuree(30);
        $offre->setDateLimite(new \DateTimeImmutable('+7 days'));
        $offre->setMailUser('test@gmail.com');

        $service = new OffreValidatorService();
        $service->validate($offre);
    }

    public function testDureeInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $offre = new Offre();

        $offre->setTitre('Offre Test');
        $offre->setPrix('150');
        $offre->setBudget('500');
        $offre->setDuree(0);
        $offre->setDateLimite(new \DateTimeImmutable('+7 days'));
        $offre->setMailUser('test@gmail.com');

        $service = new OffreValidatorService();
        $service->validate($offre);
    }

    public function testDateLimiteDansLePasse(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $offre = new Offre();

        $offre->setTitre('Offre Test');
        $offre->setPrix('150');
        $offre->setBudget('500');
        $offre->setDuree(30);
        $offre->setDateLimite(new \DateTimeImmutable('-2 days'));
        $offre->setMailUser('test@gmail.com');

        $service = new OffreValidatorService();
        $service->validate($offre);
    }

    public function testEmailUtilisateurInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $offre = new Offre();

        $offre->setTitre('Offre Test');
        $offre->setPrix('150');
        $offre->setBudget('500');
        $offre->setDuree(30);
        $offre->setDateLimite(new \DateTimeImmutable('+7 days'));
        $offre->setMailUser('email_invalide');

        $service = new OffreValidatorService();
        $service->validate($offre);
    }
}