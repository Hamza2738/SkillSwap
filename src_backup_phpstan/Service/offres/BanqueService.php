<?php

namespace App\Service\offres;

use App\Entity\Offres\Banque;
use Doctrine\ORM\EntityManagerInterface;

class BanqueService
{
    public function __construct(
        private EntityManagerInterface $entityManager
    ) {}

    public function payer(Banque $banque): void
    {
        $amount = (float) $banque->getAmount();
        $vatRate = (float) $banque->getVatRate();

        $vatAmount = ($amount * $vatRate) / 100;

        $banque->setVatAmount(number_format($vatAmount, 2, '.', ''));
        $banque->setStatut('payee');
        $banque->setDatePaiement(new \DateTime());

        $this->entityManager->persist($banque);
        $this->entityManager->flush();
    }

    public function genererOrderNumber(): string
    {
        return (string) random_int(1000000, 9999999);
    }

    public function nettoyerNumeroCarte(string $cardNumber): string
    {
        return preg_replace('/\D/', '', $cardNumber);
    }
}