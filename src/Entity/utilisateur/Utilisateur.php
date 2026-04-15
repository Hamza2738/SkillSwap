<?php

namespace App\Entity\utilisateur;

use App\Repository\UtilisateurRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;

#[ORM\Entity(repositoryClass: UtilisateurRepository::class)]
#[ORM\Table(name: 'utilisateur')]
class Utilisateur implements UserInterface, PasswordAuthenticatedUserInterface
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_utilisateur', type: 'integer')]
    private ?int $idUtilisateur = null;

    #[ORM\Column(name: 'nom', length: 100)]
    private ?string $nom = null;

    #[ORM\Column(name: 'prenom', length: 100)]
    private ?string $prenom = null;

    #[ORM\Column(name: 'email', length: 150, unique: true)]
    private ?string $email = null;

    #[ORM\Column(name: 'mot_de_passe', length: 255)]
    private ?string $motDePasse = null;

    #[ORM\Column(name: 'telephone', length: 20, nullable: true)]
    private ?string $telephone = null;

    #[ORM\Column(name: 'photo_profil', length: 500, nullable: true)]
    private ?string $photoProfil = null;

    #[ORM\Column(name: 'cle_acces', length: 255, nullable: true)]
    private ?string $cleAcces = null;

    #[ORM\Column(name: 'bio', type: Types::TEXT, nullable: true)]
    private ?string $bio = null;

    #[ORM\Column(name: 'photo_couverture', length: 500, nullable: true)]
    private ?string $photoCouverture = null;

    #[ORM\Column(name: 'lieu', length: 150, nullable: true)]
    private ?string $lieu = null;

    #[ORM\Column(name: 'role', length: 50, options: ['default' => 'freelance'])]
    private ?string $role = 'freelance';

    #[ORM\Column(name: 'statut', length: 50, options: ['default' => 'actif'])]
    private ?string $statut = 'actif';

    #[ORM\Column(name: 'date_inscription', type: Types::DATETIME_MUTABLE, options: ['default' => 'CURRENT_TIMESTAMP'])]
    private ?\DateTimeInterface $dateInscription = null;

    public function __construct()
    {
        $this->dateInscription = new \DateTime();
    }

    // ── Getters ──────────────────────────────────────────

    public function getId(): ?int { return $this->idUtilisateur; }
    public function getIdUtilisateur(): ?int { return $this->idUtilisateur; }
    public function getNom(): ?string { return $this->nom; }
    public function getPrenom(): ?string { return $this->prenom; }
    public function getEmail(): ?string { return $this->email; }
    public function getMotDePasse(): ?string { return $this->motDePasse; }
    public function getTelephone(): ?string { return $this->telephone; }
    public function getPhotoProfil(): ?string { return $this->photoProfil; }
    public function getCleAcces(): ?string { return $this->cleAcces; }
    public function getBio(): ?string { return $this->bio; }
    public function getPhotoCouverture(): ?string { return $this->photoCouverture; }
    public function getLieu(): ?string { return $this->lieu; }
    public function getRole(): ?string { return $this->role; }
    public function getStatut(): ?string { return $this->statut; }
    public function getDateInscription(): ?\DateTimeInterface { return $this->dateInscription; }

    // ── Setters ──────────────────────────────────────────

    public function setNom(string $nom): self { $this->nom = $nom; return $this; }
    public function setPrenom(string $prenom): self { $this->prenom = $prenom; return $this; }
    public function setEmail(string $email): self { $this->email = $email; return $this; }
    public function setMotDePasse(string $motDePasse): self { $this->motDePasse = $motDePasse; return $this; }
    public function setTelephone(?string $telephone): self { $this->telephone = $telephone; return $this; }
    public function setPhotoProfil(?string $photoProfil): self { $this->photoProfil = $photoProfil; return $this; }
    public function setCleAcces(?string $cleAcces): self { $this->cleAcces = $cleAcces; return $this; }
    public function setBio(?string $bio): self { $this->bio = $bio; return $this; }
    public function setPhotoCouverture(?string $photoCouverture): self { $this->photoCouverture = $photoCouverture; return $this; }
    public function setLieu(?string $lieu): self { $this->lieu = $lieu; return $this; }
    public function setRole(string $role): self { $this->role = $role; return $this; }
    public function setStatut(string $statut): self { $this->statut = $statut; return $this; }
    public function setDateInscription(\DateTimeInterface $date): self { $this->dateInscription = $date; return $this; }

    // ── Symfony Security ─────────────────────────────────

    public function getUserIdentifier(): string { return (string) $this->email; }

    // ✅ CORRIGÉ : ne retourne plus null (plantait le hasher)
    public function getPassword(): string { return $this->motDePasse ?? ''; }

    // ✅ CORRIGÉ : ROLE_ADMIN reconnu correctement par security.yaml
    public function getRoles(): array
{
    $roles = ['ROLE_USER'];

    $role = strtolower(trim((string) $this->role));

    if ($role === 'admin') {
        $roles[] = 'ROLE_ADMIN';
    } elseif ($role === 'freelancer') {
        $roles[] = 'ROLE_FREELANCER';
    } elseif ($role === 'entrepreneur') {
        $roles[] = 'ROLE_ENTREPRENEUR';
    } elseif ($role === 'freelance') {
        $roles[] = 'ROLE_FREELANCE';
    }

    return array_unique($roles);
}

    public function eraseCredentials(): void {}
}