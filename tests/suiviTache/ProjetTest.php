<?php

namespace App\Tests\Entity\suiviTache;

use App\Entity\suiviTache\Projet;
use PHPUnit\Framework\TestCase;

class ProjetTest extends TestCase
{
    public function testProjetEntity(): void
    {
        $dateDebut = new \DateTimeImmutable('2026-05-10');
        $dateFin = new \DateTimeImmutable('2026-05-20');

        $projet = new Projet();

        $projet->setTitre('Projet SkillSwap');
        $projet->setDescription('Description du projet SkillSwap');
        $projet->setDateDebut($dateDebut);
        $projet->setDateFin($dateFin);
        $projet->setStatut('En cours');
        $projet->setUserId(1);
        $projet->setMailUser('user@gmail.com');

        $this->assertNull($projet->getId());
        $this->assertSame('Projet SkillSwap', $projet->getTitre());
        $this->assertSame('Description du projet SkillSwap', $projet->getDescription());
        $this->assertSame($dateDebut, $projet->getDateDebut());
        $this->assertSame($dateFin, $projet->getDateFin());
        $this->assertSame('En cours', $projet->getStatut());
        $this->assertSame(1, $projet->getUserId());
        $this->assertSame('user@gmail.com', $projet->getMailUser());
    }

    public function testProjetNullableValues(): void
    {
        $projet = new Projet();

        $projet->setTitre(null);
        $projet->setDescription(null);
        $projet->setDateDebut(null);
        $projet->setDateFin(null);
        $projet->setStatut(null);
        $projet->setUserId(null);
        $projet->setMailUser(null);

        $this->assertNull($projet->getTitre());
        $this->assertNull($projet->getDescription());
        $this->assertNull($projet->getDateDebut());
        $this->assertNull($projet->getDateFin());
        $this->assertNull($projet->getStatut());
        $this->assertNull($projet->getUserId());
        $this->assertNull($projet->getMailUser());
    }
}