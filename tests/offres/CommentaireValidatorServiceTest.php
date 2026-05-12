<?php

namespace App\Tests\Service\offres;

use App\Entity\Offres\Commentaire;
use App\Service\offres\CommentaireValidatorService;
use PHPUnit\Framework\TestCase;

class CommentaireValidatorServiceTest extends TestCase
{
    private function createValidCommentaire(): Commentaire
    {
        $commentaire = new Commentaire();

        $commentaire->setIdOffre(1);
        $commentaire->setIdUtilisateur(2);
        $commentaire->setContenu('Très bonne offre.');
        $commentaire->setDateCommentaire(new \DateTimeImmutable('now'));

        return $commentaire;
    }

    public function testCommentaireValide(): void
    {
        $commentaire = $this->createValidCommentaire();

        $service = new CommentaireValidatorService();

        $this->assertTrue($service->validate($commentaire));
    }

    public function testIdOffreObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $commentaire = $this->createValidCommentaire();
        $commentaire->setIdOffre(null);

        $service = new CommentaireValidatorService();
        $service->validate($commentaire);
    }

    public function testIdOffreInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $commentaire = $this->createValidCommentaire();
        $commentaire->setIdOffre(0);

        $service = new CommentaireValidatorService();
        $service->validate($commentaire);
    }

    public function testIdUtilisateurObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $commentaire = $this->createValidCommentaire();
        $commentaire->setIdUtilisateur(null);

        $service = new CommentaireValidatorService();
        $service->validate($commentaire);
    }

    public function testIdUtilisateurInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $commentaire = $this->createValidCommentaire();
        $commentaire->setIdUtilisateur(0);

        $service = new CommentaireValidatorService();
        $service->validate($commentaire);
    }

    public function testContenuObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $commentaire = $this->createValidCommentaire();
        $commentaire->setContenu('');

        $service = new CommentaireValidatorService();
        $service->validate($commentaire);
    }

    public function testContenuTropCourt(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $commentaire = $this->createValidCommentaire();
        $commentaire->setContenu('a');

        $service = new CommentaireValidatorService();
        $service->validate($commentaire);
    }

    public function testDateCommentaireDansLeFutur(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $commentaire = $this->createValidCommentaire();
        $commentaire->setDateCommentaire(new \DateTimeImmutable('+1 day'));

        $service = new CommentaireValidatorService();
        $service->validate($commentaire);
    }
}