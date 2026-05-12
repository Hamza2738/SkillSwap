<?php

namespace App\Tests\Service\competence;

use App\Entity\competence\HistoriqueCompetence;
use App\Service\competence\CompetenceValidatorService;
use PHPUnit\Framework\TestCase;

/**
 * Tests unitaires pour la validation de HistoriqueCompetence.
 *
 * Règles métier testées :
 *  7. La date de modification ne peut pas être dans le futur
 *  8. Le statusChange (action) est obligatoire
 */
class HistoriqueCompetenceValidatorServiceTest extends TestCase
{
    private CompetenceValidatorService $validator;

    protected function setUp(): void
    {
        $this->validator = new CompetenceValidatorService();
    }

    // =========================================================
    // Cas valide — toutes les règles respectées
    // =========================================================

    public function testHistoriqueValideRetourneTrue(): void
    {
        $historique = $this->createHistoriqueValide();

        $result = $this->validator->validateHistorique($historique);

        $this->assertTrue($result);
    }

    // =========================================================
    // Règle 7 : Date de modification obligatoire et non future
    // =========================================================

    public function testDateModificationPasseeAcceptee(): void
    {
        $this->validator->validateDateModification(new \DateTime('-1 day'));
        $this->assertTrue(true);
    }

    public function testDateModificationAujourdhuiAcceptee(): void
    {
        $this->validator->validateDateModification(new \DateTime());
        $this->assertTrue(true);
    }

    public function testDateModificationDansFuturLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('La date de modification ne peut pas être dans le futur.');

        $historique = $this->createHistoriqueValide();
        $historique->setDateModification(new \DateTime('+1 day'));

        $this->validator->validateHistorique($historique);
    }

    public function testDateModificationNullLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('La date de modification est obligatoire.');

        $historique = $this->createHistoriqueValide();
        $historique->setDateModification(null);

        $this->validator->validateHistorique($historique);
    }

    public function testDateModificationNullDirectLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('La date de modification est obligatoire.');

        $this->validator->validateDateModification(null);
    }

    // =========================================================
    // Règle 8 : StatusChange obligatoire
    // =========================================================

    public function testStatusChangeAjouteeAccepte(): void
    {
        $this->validator->validateStatusChange('Ajoutée');
        $this->assertTrue(true);
    }

    public function testStatusChangeModifieeAccepte(): void
    {
        $this->validator->validateStatusChange('Modifiée');
        $this->assertTrue(true);
    }

    public function testStatusChangeSupprimeeAccepte(): void
    {
        $this->validator->validateStatusChange('Supprimée');
        $this->assertTrue(true);
    }

    public function testStatusChangeVideLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le statusChange (action) est obligatoire.');

        $historique = $this->createHistoriqueValide();
        $historique->setStatusChange('');

        $this->validator->validateHistorique($historique);
    }

    public function testStatusChangeNullLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le statusChange (action) est obligatoire.');

        $historique = $this->createHistoriqueValide();
        $historique->setStatusChange(null);

        $this->validator->validateHistorique($historique);
    }

    public function testStatusChangeEspacesSeulsLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le statusChange (action) est obligatoire.');

        $this->validator->validateStatusChange('   ');
    }

    // =========================================================
    // Helpers
    // =========================================================

    /**
     * Crée un HistoriqueCompetence avec toutes les données valides.
     */
    private function createHistoriqueValide(): HistoriqueCompetence
    {
        $historique = new HistoriqueCompetence();
        $historique->setCompetenceId(1);
        $historique->setCategory('Informatique');
        $historique->setType('Programmation');
        $historique->setNiveau('Intermédiaire');
        $historique->setAnneesExperience(3);
        $historique->setEmail('dev@example.com');
        $historique->setStatut('Active');
        $historique->setStatusChange('Ajoutée');
        $historique->setDateModification(new \DateTime());

        return $historique;
    }
}
