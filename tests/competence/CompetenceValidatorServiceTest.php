<?php

namespace App\Tests\Service\competence;

use App\Entity\competence\Competence;
use App\Service\competence\CompetenceValidatorService;
use PHPUnit\Framework\TestCase;

/**
 * Tests unitaires pour CompetenceValidatorService.
 *
 * Règles métier testées :
 *  1. La catégorie est obligatoire
 *  2. Le type est obligatoire
 *  3. Le niveau doit appartenir aux valeurs autorisées
 *  4. Les années d'expérience ne peuvent pas être négatives
 *  5. L'email doit être valide
 *  6. Le statut doit appartenir aux valeurs autorisées
 */
class CompetenceValidatorServiceTest extends TestCase
{
    private CompetenceValidatorService $validator;

    protected function setUp(): void
    {
        $this->validator = new CompetenceValidatorService();
    }

    // =========================================================
    // Cas valide — toutes les règles respectées
    // =========================================================

    public function testCompetenceValideRetourneTrue(): void
    {
        $competence = $this->createCompetenceValide();

        $result = $this->validator->validateCompetence($competence);

        $this->assertTrue($result);
    }

    // =========================================================
    // Règle 1 : Catégorie obligatoire
    // =========================================================

    public function testCategorieVideLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('La catégorie est obligatoire.');

        $competence = $this->createCompetenceValide();
        $competence->setCategory('');

        $this->validator->validateCompetence($competence);
    }

    public function testCategorieNullLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('La catégorie est obligatoire.');

        $this->validator->validateCategory(null);
    }

    public function testCategorieEspacesSeulsLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('La catégorie est obligatoire.');

        $this->validator->validateCategory('   ');
    }

    // =========================================================
    // Règle 2 : Type obligatoire
    // =========================================================

    public function testTypeVideLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le type est obligatoire.');

        $competence = $this->createCompetenceValide();
        $competence->setType('');

        $this->validator->validateCompetence($competence);
    }

    public function testTypeNullLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le type est obligatoire.');

        $this->validator->validateType(null);
    }

    // =========================================================
    // Règle 3 : Niveau valide
    // =========================================================

    public function testNiveauValideDebutantAccepte(): void
    {
        // Ne doit pas lever d'exception
        $this->validator->validateNiveau('Débutant');
        $this->assertTrue(true); // assertion explicite
    }

    public function testNiveauValideExpertAccepte(): void
    {
        $this->validator->validateNiveau('Expert');
        $this->assertTrue(true);
    }

    public function testNiveauInvalideLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le niveau "Master" est invalide.');

        $competence = $this->createCompetenceValide();
        $competence->setNiveau('Master');

        $this->validator->validateCompetence($competence);
    }

    public function testNiveauVideLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $this->validator->validateNiveau('');
    }

    public function testNiveauNullLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $this->validator->validateNiveau(null);
    }

    // =========================================================
    // Règle 4 : Années d'expérience non négatives
    // =========================================================

    public function testAnneesExperienceZeroAccepte(): void
    {
        $this->validator->validateAnneesExperience(0);
        $this->assertTrue(true);
    }

    public function testAnneesExperiencePositiveAccepte(): void
    {
        $this->validator->validateAnneesExperience(10);
        $this->assertTrue(true);
    }

    public function testAnneesExperienceNegativeLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Les années d\'expérience ne peuvent pas être négatives.');

        $competence = $this->createCompetenceValide();
        $competence->setAnneesExperience(-1);

        $this->validator->validateCompetence($competence);
    }

    public function testAnneesExperienceNullLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Les années d\'expérience ne peuvent pas être négatives.');

        $this->validator->validateAnneesExperience(null);
    }

    // =========================================================
    // Règle 5 : Email valide
    // =========================================================

    public function testEmailValideAccepte(): void
    {
        $this->validator->validateEmail('test@example.com');
        $this->assertTrue(true);
    }

    public function testEmailInvalideLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('L\'email est invalide.');

        $competence = $this->createCompetenceValide();
        $competence->setEmail('email_invalide');

        $this->validator->validateCompetence($competence);
    }

    public function testEmailSansArrobaseLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('L\'email est invalide.');

        $this->validator->validateEmail('emailsansarobase.com');
    }

    public function testEmailVideLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('L\'email est invalide.');

        $this->validator->validateEmail('');
    }

    public function testEmailNullLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('L\'email est invalide.');

        $this->validator->validateEmail(null);
    }

    // =========================================================
    // Règle 6 : Statut valide
    // =========================================================

    public function testStatutActiveAccepte(): void
    {
        $this->validator->validateStatut('Active');
        $this->assertTrue(true);
    }

    public function testStatutInactiveAccepte(): void
    {
        $this->validator->validateStatut('Inactive');
        $this->assertTrue(true);
    }

    public function testStatutEnAttenteAccepte(): void
    {
        $this->validator->validateStatut('En attente');
        $this->assertTrue(true);
    }

    public function testStatutInvalideLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $this->expectExceptionMessage('Le statut "Archivé" est invalide.');

        $competence = $this->createCompetenceValide();
        $competence->setStatut('Archivé');

        $this->validator->validateCompetence($competence);
    }

    public function testStatutVideLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $this->validator->validateStatut('');
    }

    public function testStatutNullLeveException(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $this->validator->validateStatut(null);
    }

    // =========================================================
    // Helpers
    // =========================================================

    /**
     * Crée une Competence avec toutes les données valides.
     */
    private function createCompetenceValide(): Competence
    {
        $competence = new Competence();
        $competence->setCategory('Informatique');
        $competence->setType('Programmation');
        $competence->setNiveau('Intermédiaire');
        $competence->setAnneesExperience(3);
        $competence->setEmail('dev@example.com');
        $competence->setStatut('Active');
        $competence->setDescription('Développement web avec Symfony');
        $competence->setCertification('Symfony Certified Developer');

        return $competence;
    }
}
