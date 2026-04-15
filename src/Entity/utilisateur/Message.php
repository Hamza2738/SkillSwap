<?php

namespace App\Entity\utilisateur;

use App\Repository\MessageRepository;
use Doctrine\ORM\Mapping as ORM;
use Doctrine\DBAL\Types\Types;

#[ORM\Entity(repositoryClass: MessageRepository::class)]
#[ORM\Table(name: 'message')]
class Message
{
    // =========================================================
    // Colonne : id INT AUTO_INCREMENT PRIMARY KEY
    // =========================================================
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id', type: Types::INTEGER)]
    private ?int $id = null;

    // =========================================================
    // Colonne : id_envoyeur INT NOT NULL
    // FK → utilisateur(id_utilisateur) ON DELETE CASCADE
    // On garde la relation ManyToOne pour Doctrine mais on
    // expose aussi getIdEnvoyeur() / setIdEnvoyeur() comme
    // dans le code Java (msg.getIdEnvoyeur() == monId)
    // =========================================================
    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_envoyeur', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $envoyeur = null;

    // =========================================================
    // Colonne : id_receveur INT NOT NULL
    // FK → utilisateur(id_utilisateur) ON DELETE CASCADE
    // =========================================================
    #[ORM\ManyToOne(targetEntity: Utilisateur::class)]
    #[ORM\JoinColumn(name: 'id_receveur', referencedColumnName: 'id_utilisateur', nullable: false, onDelete: 'CASCADE')]
    private ?Utilisateur $receveur = null;

    // =========================================================
    // Colonne : contenu TEXT
    // =========================================================
    #[ORM\Column(name: 'contenu', type: Types::TEXT, nullable: true)]
    private ?string $contenu = null;

    // =========================================================
    // Colonne : date_envoi DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    // =========================================================
    #[ORM\Column(name: 'date_envoi', type: Types::DATETIME_IMMUTABLE, nullable: false, options: ['default' => 'CURRENT_TIMESTAMP'])]
    private ?\DateTimeImmutable $dateEnvoi = null;

    // =========================================================
    // Colonne : lu BOOLEAN NOT NULL DEFAULT FALSE
    // =========================================================
    #[ORM\Column(name: 'lu', type: Types::BOOLEAN, nullable: false, options: ['default' => false])]
    private bool $lu = false;

    // =========================================================
    // Colonne : type_message VARCHAR(50) NOT NULL DEFAULT 'texte'
    // Valeurs possibles : texte | image | video | vocal | emoji | fichier
    // =========================================================
    #[ORM\Column(name: 'type_message', type: Types::STRING, length: 50, nullable: false, options: ['default' => 'texte'])]
    private string $typeMessage = 'texte';

    // =========================================================
    // Colonne : fichier_path VARCHAR(500)
    // =========================================================
    #[ORM\Column(name: 'fichier_path', type: Types::STRING, length: 500, nullable: true)]
    private ?string $fichierPath = null;

    // =========================================================
    // Colonne : fichier_nom VARCHAR(255)
    // =========================================================
    #[ORM\Column(name: 'fichier_nom', type: Types::STRING, length: 255, nullable: true)]
    private ?string $fichierNom = null;

    // =========================================================
    // Colonne : fichier_taille BIGINT
    // =========================================================
    #[ORM\Column(name: 'fichier_taille', type: Types::BIGINT, nullable: true)]
    private ?int $fichierTaille = null;

    // =========================================================
    // Colonne : duree_vocal FLOAT
    // =========================================================
    #[ORM\Column(name: 'duree_vocal', type: Types::FLOAT, nullable: true)]
    private ?float $dureeVocal = null;

    // =========================================================
    // Colonne : audio_data LONGBLOB
    // On mappe en Types::BLOB (Doctrine) → LONGBLOB MySQL
    // La valeur lue est une resource PHP (stream),
    // getAudioData() la convertit en string pour compatibilité Java.
    // =========================================================
    #[ORM\Column(name: 'audio_data', type: Types::BLOB, nullable: true)]
    private mixed $audioData = null;

    // =========================================================
    // Constructeur : initialise date_envoi automatiquement
    // (équivalent du DEFAULT CURRENT_TIMESTAMP en SQL)
    // =========================================================
    public function __construct()
    {
        $this->dateEnvoi = new \DateTimeImmutable();
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getEnvoyeur(): ?Utilisateur
    {
        return $this->envoyeur;
    }

    public function getReceveur(): ?Utilisateur
    {
        return $this->receveur;
    }

    /**
     * Raccourci utilisé partout dans ProfilController Java :
     *   msg.getIdEnvoyeur() == monId
     */
    public function getIdEnvoyeur(): ?int
    {
        return $this->envoyeur?->getIdUtilisateur();
    }

    /**
     * Raccourci symétrique pour le receveur.
     */
    public function getIdReceveur(): ?int
    {
        return $this->receveur?->getIdUtilisateur();
    }

    public function getContenu(): ?string
    {
        return $this->contenu;
    }

    public function getDateEnvoi(): ?\DateTimeImmutable
    {
        return $this->dateEnvoi;
    }

    public function isLu(): bool
    {
        return $this->lu;
    }

    public function getTypeMessage(): string
    {
        return $this->typeMessage;
    }

    public function getFichierPath(): ?string
    {
        return $this->fichierPath;
    }

    public function getFichierNom(): ?string
    {
        return $this->fichierNom;
    }

    /**
     * fichier_taille est BIGINT en SQL.
     * PHP le lit comme string sur certaines plateformes 32-bit,
     * on cast en int pour rester compatible avec le code Java (long).
     */
    public function getFichierTaille(): ?int
    {
        return $this->fichierTaille !== null ? (int) $this->fichierTaille : null;
    }

    public function getDureeVocal(): ?float
    {
        return $this->dureeVocal;
    }

    /**
     * audio_data est un LONGBLOB → Doctrine retourne une resource (stream).
     * On le convertit en string binaire pour être équivalent au byte[]
     * du code Java (msg.getAudioData()).
     */
    public function getAudioData(): ?string
    {
        if ($this->audioData === null) {
            return null;
        }
        // Si Doctrine a retourné un stream (resource)
        if (is_resource($this->audioData)) {
            return stream_get_contents($this->audioData);
        }
        // Déjà une string (cas test unitaire ou valeur injectée manuellement)
        return $this->audioData;
    }

    // =========================================================
    // SETTERS
    // =========================================================

    public function setEnvoyeur(?Utilisateur $u): static
    {
        $this->envoyeur = $u;
        return $this;
    }

    public function setReceveur(?Utilisateur $u): static
    {
        $this->receveur = $u;
        return $this;
    }

    public function setContenu(?string $c): static
    {
        $this->contenu = $c;
        return $this;
    }

    public function setDateEnvoi(\DateTimeImmutable $d): static
    {
        $this->dateEnvoi = $d;
        return $this;
    }

    public function setLu(bool $lu): static
    {
        $this->lu = $lu;
        return $this;
    }

    public function setTypeMessage(string $t): static
    {
        $this->typeMessage = $t;
        return $this;
    }

    public function setFichierPath(?string $p): static
    {
        $this->fichierPath = $p;
        return $this;
    }

    public function setFichierNom(?string $n): static
    {
        $this->fichierNom = $n;
        return $this;
    }

    public function setFichierTaille(?int $t): static
    {
        $this->fichierTaille = $t;
        return $this;
    }

    public function setDureeVocal(?float $d): static
    {
        $this->dureeVocal = $d;
        return $this;
    }

    /**
     * Accepte string ou resource (stream).
     * Équivalent de msg.setAudioData(byte[]) en Java.
     */
    public function setAudioData(mixed $d): static
    {
        $this->audioData = $d;
        return $this;
    }

    // =========================================================
    // HELPERS — compatibilité avec le code Java et le controller
    // =========================================================

    /** msg.getTypeMessage() === "texte" ou null */
    public function isTexte(): bool
    {
        return $this->typeMessage === 'texte' || $this->typeMessage === null;
    }

    public function isImage(): bool   { return $this->typeMessage === 'image'; }
    public function isVideo(): bool   { return $this->typeMessage === 'video'; }
    public function isVocal(): bool   { return $this->typeMessage === 'vocal'; }
    public function isEmoji(): bool   { return $this->typeMessage === 'emoji'; }
    public function isFichier(): bool { return $this->typeMessage === 'fichier'; }

    /**
     * hasFichier() — équivalent exact de msg.hasFichier() en Java :
     *   return fichierPath != null && !fichierPath.isBlank()
     */
    public function hasFichier(): bool
    {
        return $this->fichierPath !== null && trim($this->fichierPath) !== '';
    }
}