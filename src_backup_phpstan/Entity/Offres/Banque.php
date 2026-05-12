<?php

namespace App\Entity\Offres;

use App\Repository\BanqueRepository;
use App\Entity\Offres\Offre;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: BanqueRepository::class)]
#[ORM\Table(name: 'banque')]
class Banque
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_banque', type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Offre::class)]
    #[ORM\JoinColumn(name: 'id_offre', referencedColumnName: 'id_offre', nullable: false, onDelete: 'CASCADE')]
    private ?Offre $offre = null;

    #[ORM\Column(name: 'card_number', type: 'string', length: 30)]
    private ?string $cardNumber = null;

    #[ORM\Column(name: 'card_holder', type: 'string', length: 150, nullable: true)]
    private ?string $cardHolder = null;

    #[ORM\Column(name: 'cvv', type: 'string', length: 10)]
    private ?string $cvv = null;

    #[ORM\Column(name: 'expiry_month', type: 'string', length: 2)]
    private ?string $expiryMonth = null;

    #[ORM\Column(name: 'expiry_year', type: 'string', length: 4)]
    private ?string $expiryYear = null;

    #[ORM\Column(name: 'dynamic_password', type: 'string', length: 255, nullable: true)]
    private ?string $dynamicPassword = null;

    #[ORM\Column(name: 'company', type: 'string', length: 150, nullable: true)]
    private ?string $company = null;

    #[ORM\Column(name: 'order_number', type: 'string', length: 100, nullable: true)]
    private ?string $orderNumber = null;

    #[ORM\Column(name: 'product', type: 'string', length: 200, nullable: true)]
    private ?string $product = null;

    #[ORM\Column(name: 'vat_rate', type: 'decimal', precision: 5, scale: 2)]
    private ?string $vatRate = '20.00';

    #[ORM\Column(name: 'vat_amount', type: 'decimal', precision: 12, scale: 2)]
    private ?string $vatAmount = '0.00';

    #[ORM\Column(name: 'amount', type: 'decimal', precision: 12, scale: 2)]
    private ?string $amount = null;

    #[ORM\Column(name: 'currency', type: 'string', length: 10)]
    private ?string $currency = 'USD';

    #[ORM\Column(name: 'statut', type: 'string', length: 50)]
    private ?string $statut = 'en_attente';

    #[ORM\Column(name: 'date_creation', type: 'datetime')]
    private ?\DateTimeInterface $dateCreation = null;

    #[ORM\Column(name: 'date_paiement', type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $datePaiement = null;

    public function __construct()
    {
        $this->dateCreation = new \DateTime();
        $this->statut = 'en_attente';
        $this->currency = 'USD';
        $this->vatRate = '20.00';
        $this->vatAmount = '0.00';
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getOffre(): ?Offre
    {
        return $this->offre;
    }

    public function setOffre(?Offre $offre): self
    {
        $this->offre = $offre;
        return $this;
    }

    public function getCardNumber(): ?string
    {
        return $this->cardNumber;
    }

    public function setCardNumber(?string $cardNumber): self
    {
        $this->cardNumber = $cardNumber;
        return $this;
    }

    public function getCardHolder(): ?string
    {
        return $this->cardHolder;
    }

    public function setCardHolder(?string $cardHolder): self
    {
        $this->cardHolder = $cardHolder;
        return $this;
    }

    public function getCvv(): ?string
    {
        return $this->cvv;
    }

    public function setCvv(?string $cvv): self
    {
        $this->cvv = $cvv;
        return $this;
    }

    public function getExpiryMonth(): ?string
    {
        return $this->expiryMonth;
    }

    public function setExpiryMonth(?string $expiryMonth): self
    {
        $this->expiryMonth = $expiryMonth;
        return $this;
    }

    public function getExpiryYear(): ?string
    {
        return $this->expiryYear;
    }

    public function setExpiryYear(?string $expiryYear): self
    {
        $this->expiryYear = $expiryYear;
        return $this;
    }

    public function getDynamicPassword(): ?string
    {
        return $this->dynamicPassword;
    }

    public function setDynamicPassword(?string $dynamicPassword): self
    {
        $this->dynamicPassword = $dynamicPassword;
        return $this;
    }

    public function getCompany(): ?string
    {
        return $this->company;
    }

    public function setCompany(?string $company): self
    {
        $this->company = $company;
        return $this;
    }

    public function getOrderNumber(): ?string
    {
        return $this->orderNumber;
    }

    public function setOrderNumber(?string $orderNumber): self
    {
        $this->orderNumber = $orderNumber;
        return $this;
    }

    public function getProduct(): ?string
    {
        return $this->product;
    }

    public function setProduct(?string $product): self
    {
        $this->product = $product;
        return $this;
    }

    public function getVatRate(): ?string
    {
        return $this->vatRate;
    }

    public function setVatRate(?string $vatRate): self
    {
        $this->vatRate = $vatRate;
        return $this;
    }

    public function getVatAmount(): ?string
    {
        return $this->vatAmount;
    }

    public function setVatAmount(?string $vatAmount): self
    {
        $this->vatAmount = $vatAmount;
        return $this;
    }

    public function getAmount(): ?string
    {
        return $this->amount;
    }

    public function setAmount(?string $amount): self
    {
        $this->amount = $amount;
        return $this;
    }

    public function getCurrency(): ?string
    {
        return $this->currency;
    }

    public function setCurrency(?string $currency): self
    {
        $this->currency = $currency;
        return $this;
    }

    public function getStatut(): ?string
    {
        return $this->statut;
    }

    public function setStatut(?string $statut): self
    {
        $this->statut = $statut;
        return $this;
    }

    public function getDateCreation(): ?\DateTimeInterface
    {
        return $this->dateCreation;
    }

    public function setDateCreation(?\DateTimeInterface $dateCreation): self
    {
        $this->dateCreation = $dateCreation;
        return $this;
    }

    public function getDatePaiement(): ?\DateTimeInterface
    {
        return $this->datePaiement;
    }

    public function setDatePaiement(?\DateTimeInterface $datePaiement): self
    {
        $this->datePaiement = $datePaiement;
        return $this;
    }

    public function getMaskedCardNumber(): string
    {
        $clean = preg_replace('/\D/', '', $this->cardNumber ?? '');

        if (strlen($clean) < 4) {
            return '••••';
        }

        return '••••  ' . substr($clean, -4);
    }
}