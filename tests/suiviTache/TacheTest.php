<?php

namespace App\Tests\Entity\suiviTache;

use App\Entity\suiviTache\Tache;
use PHPUnit\Framework\TestCase;

class TacheTest extends TestCase
{
    public function testTacheEntity(): void
    {
        $echeance = new \DateTimeImmutable('2026-05-20');

        $tache = new Tache();

        $tache->setTitre('Créer interface projet');
        $tache->setDescription('Développer la partie front de la gestion des projets');
        $tache->setStatut('En cours');
        $tache->setPriorite('Haute');
        $tache->setEcheance($echeance);
        $tache->setProjetId(1);
        $tache->setUserId(2);
        $tache->setMailUser('user@gmail.com');
        $tache->setEvaluation('Bonne progression');

        $this->assertNull($tache->getId());
        $this->assertSame('Créer interface projet', $tache->getTitre());
        $this->assertSame('Développer la partie front de la gestion des projets', $tache->getDescription());
        $this->assertSame('En cours', $tache->getStatut());
        $this->assertSame('Haute', $tache->getPriorite());
        $this->assertSame($echeance, $tache->getEcheance());
        $this->assertSame(1, $tache->getProjetId());
        $this->assertSame(2, $tache->getUserId());
        $this->assertSame('user@gmail.com', $tache->getMailUser());
        $this->assertSame('Bonne progression', $tache->getEvaluation());
    }

    public function testTacheNullableValues(): void
    {
        $tache = new Tache();

        $tache->setTitre(null);
        $tache->setDescription(null);
        $tache->setStatut(null);
        $tache->setPriorite(null);
        $tache->setEcheance(null);
        $tache->setProjetId(null);
        $tache->setUserId(null);
        $tache->setMailUser(null);
        $tache->setEvaluation(null);

        $this->assertNull($tache->getTitre());
        $this->assertNull($tache->getDescription());
        $this->assertNull($tache->getStatut());
        $this->assertNull($tache->getPriorite());
        $this->assertNull($tache->getEcheance());
        $this->assertNull($tache->getProjetId());
        $this->assertNull($tache->getUserId());
        $this->assertNull($tache->getMailUser());
        $this->assertNull($tache->getEvaluation());
    }
}