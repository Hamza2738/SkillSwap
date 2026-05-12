<?php

namespace App\Tests\Service\offres;

use App\Entity\Offres\OffreLike;
use App\Service\offres\OffreLikeValidatorService;
use PHPUnit\Framework\TestCase;

class OffreLikeValidatorServiceTest extends TestCase
{
    private function createValidOffreLike(): OffreLike
    {
        $offreLike = new OffreLike();

        $offreLike->setIdOffre(1);
        $offreLike->setIdUtilisateur(2);

        return $offreLike;
    }

    public function testOffreLikeValide(): void
    {
        $offreLike = $this->createValidOffreLike();

        $service = new OffreLikeValidatorService();

        $this->assertTrue($service->validate($offreLike));
    }

    public function testIdOffreObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $offreLike = $this->createValidOffreLike();
        $offreLike->setIdOffre(null);

        $service = new OffreLikeValidatorService();
        $service->validate($offreLike);
    }

    public function testIdOffreInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $offreLike = $this->createValidOffreLike();
        $offreLike->setIdOffre(0);

        $service = new OffreLikeValidatorService();
        $service->validate($offreLike);
    }

    public function testIdUtilisateurObligatoire(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $offreLike = $this->createValidOffreLike();
        $offreLike->setIdUtilisateur(null);

        $service = new OffreLikeValidatorService();
        $service->validate($offreLike);
    }

    public function testIdUtilisateurInvalide(): void
    {
        $this->expectException(\InvalidArgumentException::class);

        $offreLike = $this->createValidOffreLike();
        $offreLike->setIdUtilisateur(0);

        $service = new OffreLikeValidatorService();
        $service->validate($offreLike);
    }
}