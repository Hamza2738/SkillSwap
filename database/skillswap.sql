-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : sam. 16 mai 2026 à 15:38
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `skillswap`
--

-- --------------------------------------------------------

--
-- Structure de la table `banque`
--

CREATE TABLE `banque` (
  `id_banque` int(11) NOT NULL,
  `id_offre` int(11) NOT NULL,
  `card_number` varchar(30) NOT NULL,
  `card_holder` varchar(150) DEFAULT NULL,
  `cvv` varchar(10) NOT NULL,
  `expiry_month` varchar(2) NOT NULL,
  `expiry_year` varchar(4) NOT NULL,
  `dynamic_password` varchar(255) DEFAULT NULL,
  `company` varchar(150) DEFAULT NULL,
  `order_number` varchar(100) DEFAULT NULL,
  `product` varchar(200) DEFAULT NULL,
  `vat_rate` decimal(5,2) NOT NULL DEFAULT 20.00,
  `vat_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `amount` decimal(12,2) NOT NULL,
  `currency` varchar(10) NOT NULL DEFAULT 'USD',
  `statut` varchar(50) NOT NULL DEFAULT 'en_attente',
  `date_creation` datetime NOT NULL DEFAULT current_timestamp(),
  `date_paiement` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `banque`
--

INSERT INTO `banque` (`id_banque`, `id_offre`, `card_number`, `card_holder`, `cvv`, `expiry_month`, `expiry_year`, `dynamic_password`, `company`, `order_number`, `product`, `vat_rate`, `vat_amount`, `amount`, `currency`, `statut`, `date_creation`, `date_paiement`) VALUES
(1, 2, '64326386', 'yassine allala', '456', '12', '75', '7896321', 'SkillSwap', '3368805', 'am', 20.00, 300.00, 1500.00, 'TND', 'payee', '2026-05-12 12:21:57', '2026-05-12 12:21:57'),
(2, 3, '123456789', 'yassineaeeee allala', '775', '20', '14', '789456', 'SkillSwap', '5713754', 'validation', 20.00, 4000.00, 20000.00, 'TND', 'payee', '2026-05-13 18:20:56', '2026-05-13 18:20:56');

-- --------------------------------------------------------

--
-- Structure de la table `candidature`
--

CREATE TABLE `candidature` (
  `id_candidature` int(11) NOT NULL,
  `date_candidature` datetime DEFAULT NULL,
  `date_abonnement` datetime DEFAULT NULL,
  `date_expiration` datetime DEFAULT NULL,
  `matricule_offre` varchar(50) DEFAULT NULL,
  `mail_acheteur` varchar(150) DEFAULT NULL,
  `prix_paye` decimal(12,2) DEFAULT NULL,
  `mode_paiement` varchar(50) DEFAULT NULL,
  `reference_paiement` varchar(255) DEFAULT NULL,
  `statut` varchar(50) DEFAULT NULL,
  `message` text DEFAULT NULL,
  `cv` varchar(255) DEFAULT NULL,
  `lettre_motivation` varchar(255) DEFAULT NULL,
  `note_recruteur` text DEFAULT NULL,
  `note_candidat` int(11) DEFAULT NULL CHECK (`note_candidat` between 1 and 5),
  `is_active` tinyint(1) DEFAULT 1,
  `id_utilisateur` int(11) NOT NULL,
  `id_offre` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `candidature`
--

INSERT INTO `candidature` (`id_candidature`, `date_candidature`, `date_abonnement`, `date_expiration`, `matricule_offre`, `mail_acheteur`, `prix_paye`, `mode_paiement`, `reference_paiement`, `statut`, `message`, `cv`, `lettre_motivation`, `note_recruteur`, `note_candidat`, `is_active`, `id_utilisateur`, `id_offre`) VALUES
(1, '2026-05-12 10:56:02', '2026-05-12 10:56:02', '2026-07-19 10:56:02', 'OFF-2026-00001', 'yassine.alla@gmail.com', 10.00, 'carte', 'REF-B6EEA748', 'en_attente', '', NULL, '', NULL, NULL, 1, 12, 1),
(2, '2026-05-12 12:21:57', '2026-05-12 12:21:57', '2026-07-11 12:21:57', 'OFF-2026-00002', 'yassine.alla@gmail.com', 1500.00, 'carte', '3368805', 'payee', '', NULL, '', NULL, NULL, 1, 12, 2),
(3, '2026-05-13 18:20:56', '2026-05-13 18:20:56', '2026-05-23 18:20:56', 'OFF-2026-00003', 'yassine.alla@gmail.com', 20000.00, 'paypal', '5713754', 'payee', 'motivée sur cette foramtion', NULL, 'je suis un etudiant ', NULL, NULL, 1, 12, 3);

-- --------------------------------------------------------

--
-- Structure de la table `commentaire`
--

CREATE TABLE `commentaire` (
  `id_commentaire` int(11) NOT NULL,
  `id_offre` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `contenu` text NOT NULL,
  `date_commentaire` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `competences`
--

CREATE TABLE `competences` (
  `id` int(11) NOT NULL,
  `category` varchar(100) DEFAULT NULL,
  `type` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `niveau` varchar(50) DEFAULT 'Débutant',
  `annees_experience` int(11) DEFAULT 0,
  `certification` varchar(255) DEFAULT NULL,
  `statut` varchar(50) DEFAULT 'En cours',
  `email` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `competences`
--

INSERT INTO `competences` (`id`, `category`, `type`, `description`, `niveau`, `annees_experience`, `certification`, `statut`, `email`) VALUES
(5, 'java', 'java', 'test', 'Avancé', 1, 'dazaxQ', 'Validée', NULL),
(6, 'java', 'java', 'java', 'Intermédiaire', 5, 'dazaxQ', 'Expirée', 'yassine.alla@gmail.com'),
(7, 'dev', 'java', 'developemnt desktoop', 'Intermédiaire', 1, 'microsoft', 'Validée', 'yassine.alla@gmail.com'),
(8, 'test', 'ysys', 'test', 'Avancé', 1, 'dazaxQ', 'En cours', NULL),
(10, 'java', 'java', 'test', 'Avancé', 4, 'dazaxQ', 'En cours', NULL),
(11, 'test', 'ysys', 'test', 'Débutant', 1, 'dazaxQ', 'En cours', NULL),
(12, 'Réseaux Télécom', 'Technique', 'Maîtrise des concepts liés aux réseaux de télécommunication, à l’architecture radio/mobile, à la supervision réseau et à l’optimisation des performances. Capacité à comprendre les environnements opérateurs et les infrastructures télécom.', 'Avancé', 3, 'Nokia / CCNA636895', 'En cours', 'chahine.belhaj@gmail.com'),
(13, 'Cybersécurité', 'Technique', 'Connaissance des principes de cybersécurité, de protection des systèmes, du contrôle d’accès, des vulnérabilités et des bonnes pratiques de sécurisation des applications et réseaux. Capacité à identifier et réduire les risques de sécurité.', 'Débutant', 2, 'Security+ / CEH', 'Validée', 'chahine.belhaj@gmail.com');

-- --------------------------------------------------------

--
-- Structure de la table `connexion`
--

CREATE TABLE `connexion` (
  `id` int(11) NOT NULL,
  `id_demandeur` int(11) NOT NULL,
  `id_receveur` int(11) NOT NULL,
  `statut` varchar(50) DEFAULT NULL,
  `date_demande` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `connexion`
--

INSERT INTO `connexion` (`id`, `id_demandeur`, `id_receveur`, `statut`, `date_demande`) VALUES
(1, 10, 11, 'accepte', NULL),
(5, 11, 12, 'accepte', NULL),
(6, 25, 11, 'accepte', '2026-05-12 14:07:27'),
(7, 26, 11, 'accepte', NULL),
(9, 26, 10, 'en_attente', NULL),
(10, 27, 11, 'accepte', NULL),
(12, 11, 30, 'accepte', NULL),
(13, 31, 11, 'accepte', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `historiques_competences`
--

CREATE TABLE `historiques_competences` (
  `id` int(11) NOT NULL,
  `competence_id` int(11) NOT NULL,
  `category` varchar(100) DEFAULT NULL,
  `type` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `niveau` varchar(50) DEFAULT NULL,
  `annees_experience` int(11) DEFAULT NULL,
  `certification` varchar(255) DEFAULT NULL,
  `statut` varchar(50) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `status_change` varchar(100) DEFAULT NULL,
  `date_modification` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `historiques_competences`
--

INSERT INTO `historiques_competences` (`id`, `competence_id`, `category`, `type`, `description`, `niveau`, `annees_experience`, `certification`, `statut`, `email`, `status_change`, `date_modification`) VALUES
(19, 12, 'Réseaux Télécom', 'Technique', 'Maîtrise des concepts liés aux réseaux de télécommunication, à l’architecture radio/mobile, à la supervision réseau et à l’optimisation des performances. Capacité à comprendre les environnements opérateurs et les infrastructures télécom.', 'Avancé', 3, 'Nokia / CCNA636895', 'En cours', 'chahine.belhaj@gmail.com', 'Modifiée', '2026-05-13 14:57:15'),
(20, 7, 'dev', 'java', 'developemnt desktoop', 'Intermédiaire', 1, 'microsoft', 'Validée', 'yassine.alla@gmail.com', 'Modifiée', '2026-05-13 14:59:51');

-- --------------------------------------------------------

--
-- Structure de la table `message`
--

CREATE TABLE `message` (
  `id` int(11) NOT NULL,
  `id_envoyeur` int(11) NOT NULL,
  `id_receveur` int(11) NOT NULL,
  `contenu` text DEFAULT NULL,
  `date_envoi` datetime DEFAULT NULL,
  `lu` tinyint(1) DEFAULT 0,
  `type_message` varchar(50) DEFAULT 'texte',
  `fichier_path` varchar(255) DEFAULT NULL,
  `fichier_nom` varchar(255) DEFAULT NULL,
  `fichier_taille` bigint(20) DEFAULT NULL,
  `duree_vocal` float DEFAULT NULL,
  `audio_data` longblob DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `message`
--

INSERT INTO `message` (`id`, `id_envoyeur`, `id_receveur`, `contenu`, `date_envoi`, `lu`, `type_message`, `fichier_path`, `fichier_nom`, `fichier_taille`, `duree_vocal`, `audio_data`) VALUES
(2, 10, 11, 'cc', NULL, 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(3, 11, 10, 'amam', NULL, 0, 'texte', NULL, NULL, NULL, NULL, NULL),
(4, 11, 31, 'cc', '2026-05-13 00:41:10', 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(5, 11, 31, 'cc', '2026-05-13 00:41:14', 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(6, 11, 31, 'cc', '2026-05-13 00:41:18', 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(7, 11, 31, 'cc', '2026-05-13 00:41:22', 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(8, 11, 31, 'cc', '2026-05-13 00:41:26', 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(9, 11, 31, 'cc', '2026-05-13 00:41:30', 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(10, 11, 31, 'cc', '2026-05-13 00:41:40', 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(11, 11, 31, 'cc', '2026-05-13 00:41:47', 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(12, 11, 31, 'cc', '2026-05-13 00:41:58', 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(13, 31, 11, 'salut', NULL, 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(14, 11, 26, 'bonsoir', '2026-05-13 00:44:54', 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(15, 26, 11, 'salut', NULL, 1, 'texte', NULL, NULL, NULL, NULL, NULL),
(16, 12, 11, 'salut', '2026-05-13 18:19:20', 0, 'texte', NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `offre`
--

CREATE TABLE `offre` (
  `id_offre` int(11) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `type_offre` varchar(50) DEFAULT NULL,
  `budget` decimal(12,2) DEFAULT NULL,
  `prix` decimal(12,2) DEFAULT NULL,
  `duree` int(11) DEFAULT NULL,
  `matricule` varchar(50) DEFAULT NULL,
  `localisation` varchar(150) DEFAULT NULL,
  `date_publication` date DEFAULT NULL,
  `date_limite` date DEFAULT NULL,
  `statut` varchar(50) DEFAULT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `mail_user` varchar(150) DEFAULT NULL,
  `vues` int(11) DEFAULT 0,
  `tags` varchar(500) DEFAULT NULL,
  `nb_likes` int(11) DEFAULT 0,
  `nb_commentaires` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `offre`
--

INSERT INTO `offre` (`id_offre`, `titre`, `description`, `type_offre`, `budget`, `prix`, `duree`, `matricule`, `localisation`, `date_publication`, `date_limite`, `statut`, `id_utilisateur`, `mail_user`, `vues`, `tags`, `nb_likes`, `nb_commentaires`) VALUES
(1, 'tedzsqx', 'aef', 'projet', 27.00, 10.00, 68, 'OFF-2026-00001', 'mesr', '2026-04-12', '2026-04-23', 'ouverte', 11, NULL, 1, 'java', 2, 0),
(2, 'am', 'sa', 'emploi', 1256.00, 1500.00, 60, 'OFF-2026-00002', 'France', '2026-05-12', '2026-06-07', 'ouverte', 11, NULL, 0, 'java', 0, 0),
(3, 'validation', 'offre val', 'projet', 12.00, 20000.00, 10, 'OFF-2026-00003', 'bizerte', '2026-05-12', '2026-05-14', 'ouverte', 11, NULL, 0, 'java', 0, 0);

-- --------------------------------------------------------

--
-- Structure de la table `offre_like`
--

CREATE TABLE `offre_like` (
  `id` int(11) NOT NULL,
  `id_offre` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `offre_like`
--

INSERT INTO `offre_like` (`id`, `id_offre`, `id_utilisateur`) VALUES
(5, 1, 11),
(4, 1, 12);

-- --------------------------------------------------------

--
-- Structure de la table `postulation`
--

CREATE TABLE `postulation` (
  `id_postulation` int(11) NOT NULL,
  `id_rendez_vous` int(11) NOT NULL,
  `id_postulant` int(11) NOT NULL,
  `mail_user` varchar(150) DEFAULT NULL,
  `message` text DEFAULT NULL,
  `cv` varchar(255) DEFAULT NULL,
  `status` varchar(50) DEFAULT 'en_attente',
  `date_postulation` datetime DEFAULT NULL,
  `role` varchar(50) DEFAULT NULL,
  `nom` varchar(100) DEFAULT NULL,
  `prenom` varchar(100) DEFAULT NULL,
  `titre` varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `postulation`
--

INSERT INTO `postulation` (`id_postulation`, `id_rendez_vous`, `id_postulant`, `mail_user`, `message`, `cv`, `status`, `date_postulation`, `role`, `nom`, `prenom`, `titre`) VALUES
(2, 4, 11, 'bessrour.mehdi@gmail.com', 'FDGH', 'C:\\Users\\hamza\\Downloads\\Prétraitement_des_données (1).pdf', 'refuse', NULL, 'admin', 'mehdi', 'bessrour', 'Initiation au marketing digital'),
(3, 1, 12, 'yassine.alla@gmail.com', 'NK?./', 'uploads/cv/cv_6a032bd14fec05.14657343.pdf', 'en_attente', '2026-05-12 15:32:01', 'entrepreneur', 'yassine', 'allala', 'Préparation CV et entretien professionnel');

-- --------------------------------------------------------

--
-- Structure de la table `projet`
--

CREATE TABLE `projet` (
  `id` int(11) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `date_debut` date DEFAULT NULL,
  `date_fin` date DEFAULT NULL,
  `statut` varchar(50) DEFAULT NULL,
  `user_id` int(11) NOT NULL,
  `mail_user` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `projet`
--

INSERT INTO `projet` (`id`, `titre`, `description`, `date_debut`, `date_fin`, `statut`, `user_id`, `mail_user`) VALUES
(2, 'Système de gestion et suivi des compétences', 'Mise en place d’un module permettant d’ajouter, modifier, rechercher et organiser les compétences des utilisateurs avec une interface simple, moderne et adaptée aux besoins professionnels.', '2026-04-23', '2026-04-23', 'En attente', 11, 'bessrour.mehdi@gmail.com'),
(3, 'Développement d’une plateforme de réservation en ligne', 'Création d’une application web permettant aux utilisateurs de réserver des rendez-vous, consulter les disponibilités, recevoir des confirmations et gérer leurs demandes depuis un espace personnel.', '2026-04-11', '2026-04-23', 'En cours', 11, 'bessrour.mehdi@gmail.com'),
(4, 'test', 'test', '2026-05-13', '2026-05-31', 'En cours', 11, 'bessrour.mehdi@gmail.com'),
(5, 'test validation web symfony', 'jhkfkjhkjdhsfjl', '2026-05-20', '2026-05-29', 'En cours', 11, 'bessrour.mehdi@gmail.com');

-- --------------------------------------------------------

--
-- Structure de la table `rendez_vous`
--

CREATE TABLE `rendez_vous` (
  `id_rendez_vous` int(11) NOT NULL,
  `id_admin_createur` int(11) NOT NULL,
  `mail_user` varchar(150) DEFAULT NULL,
  `titre` varchar(200) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `date_rendez_vous` datetime DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  `competence` varchar(150) DEFAULT NULL,
  `date_creation_rendez_vous` datetime DEFAULT NULL,
  `nombre_places` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `rendez_vous`
--

INSERT INTO `rendez_vous` (`id_rendez_vous`, `id_admin_createur`, `mail_user`, `titre`, `description`, `date_rendez_vous`, `type`, `competence`, `date_creation_rendez_vous`, `nombre_places`) VALUES
(1, 11, 'bessrour.mehdi@gmail.com', 'Préparation CV et entretien professionnel', 'Séance d’accompagnement pour améliorer un CV, préparer une présentation professionnelle et s’entraîner aux questions fréquentes d’entretien.	', '2027-05-02 09:00:00', 'presentiel', 'Communication professionnelle', NULL, 10),
(2, 11, 'bessrour.mehdi@gmail.com', 'Méthodes de gestion de projet', 'Rendez-vous destiné à présenter les bases de la gestion de projet : planification, répartition des tâches, suivi de l’avancement, gestion des délais et collaboration en équipe.', '2027-04-17 08:00:00', 'distance', 'Gestion de projet', '2026-05-12 12:16:52', 51),
(3, 12, 'yassine.alla@gmail.com', 'Accompagnement création de projet', 'Séance de conseil destinée aux personnes souhaitant lancer un projet entrepreneurial. Nous allons discuter de l’idée, du marché cible, du modèle économique et des premières étapes à suivre pour démarrer correctement.', '2026-05-23 08:00:00', 'presentiel', 'Entrepreneuriat', '2026-05-12 13:53:36', 5),
(4, 12, 'yassine.alla@gmail.com', 'Initiation au marketing digital', 'Rendez-vous de formation pour découvrir les bases du marketing digital : réseaux sociaux, publicité en ligne, stratégie de contenu et analyse des performances.', '2026-05-31 12:04:00', 'presentiel', 'Marketing digital', '2026-05-12 13:56:05', 10),
(5, 12, 'yassine.alla@gmail.com', 'Atelier Symfony pour débutants', 'Atelier pratique pour apprendre les bases du framework Symfony, la structure MVC, la création de routes, contrôleurs, entités et templates Twig.', '2026-05-23 08:00:00', 'distance', 'Développement web', '2026-05-12 13:56:45', 50),
(6, 11, 'bessrour.mehdi@gmail.com', 'tetsgb', 'Rendez-vous destiné à présenter les bases de la gestion de projet : planification, répartition des tâches, suivi de l’avancement, gestion des délais et collaboration en équipe.', '2027-04-17 08:00:00', 'distance', 'Gestion de projet', '2026-05-12 14:59:55', 51),
(7, 11, 'bessrour.mehdi@gmail.com', 'jfytey', 'fugfhtf', '2026-05-23 09:00:00', 'presentiel', 'jyfufht', NULL, 2),
(8, 12, 'yassine.alla@gmail.com', 'hamzaa', 'utfytfyt', '2026-05-21 09:00:00', 'presentiel', 'java', NULL, 2),
(9, 12, 'yassine.alla@gmail.com', 'GFH', 'GHJ', '2026-04-08 15:08:00', 'presentiel', 'KKL', '2026-05-12 16:09:57', 22),
(10, 11, 'hamzanajjar273@gmail.com', 'formation', 'formation java et php', '2026-05-23 08:30:00', 'presentiel', 'developement web desktop', NULL, 5),
(11, 11, 'hamzanajjar273@gmail.com', 'formationjava', 'formation proffesionel java certifiee', '2026-05-21 09:00:00', 'presentiel', 'java', NULL, 5);

-- --------------------------------------------------------

--
-- Structure de la table `tache`
--

CREATE TABLE `tache` (
  `id` int(11) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `statut` varchar(50) DEFAULT NULL,
  `priorite` varchar(50) DEFAULT NULL,
  `echeance` date DEFAULT NULL,
  `projet_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `mail_user` varchar(150) DEFAULT NULL,
  `evaluation` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `tache`
--

INSERT INTO `tache` (`id`, `titre`, `description`, `statut`, `priorite`, `echeance`, `projet_id`, `user_id`, `mail_user`, `evaluation`) VALUES
(2, 'Créer la page de réservation', 'Développer une interface permettant aux utilisateurs de choisir une date, une heure et un type de rendez-vous disponible.', 'En cours', 'Moyenne', '2026-05-15', 3, 11, 'bessrour.mehdi@gmail.com', ''),
(3, 'Ajouter la confirmation de réservation', 'Mettre en place un message de confirmation après chaque réservation avec les détails du rendez-vous sélectionné.', 'À faire', 'Basse', '2026-05-31', 2, 11, 'bessrour.mehdi@gmail.com', ''),
(4, 'DZA', 'DZA', 'À faire', 'Basse', '2026-12-25', 4, 12, 'yassine.alla@gmail.com', NULL),
(5, 'web java', 'ifoidgjlkjg', 'Terminée', 'Moyenne', '2026-05-15', 5, 11, 'bessrour.mehdi@gmail.com', 'Bien');

-- --------------------------------------------------------

--
-- Structure de la table `utilisateur`
--

CREATE TABLE `utilisateur` (
  `id_utilisateur` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `photo_profil` varchar(255) DEFAULT NULL,
  `cle_acces` varchar(255) DEFAULT NULL,
  `bio` text DEFAULT NULL,
  `photo_couverture` varchar(255) DEFAULT NULL,
  `lieu` varchar(150) DEFAULT NULL,
  `role` varchar(50) DEFAULT NULL,
  `statut` varchar(50) DEFAULT NULL,
  `date_inscription` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `utilisateur`
--

INSERT INTO `utilisateur` (`id_utilisateur`, `nom`, `prenom`, `email`, `mot_de_passe`, `telephone`, `photo_profil`, `cle_acces`, `bio`, `photo_couverture`, `lieu`, `role`, `statut`, `date_inscription`) VALUES
(10, 'chahine', 'belhaj', 'chahine.belhaj@gmail.com', '555555', '+216 2788369123', '/view/image/utilisateur/uploads/u_10_d9afad76-fc51-4560-8567-ca87ca06b4a7.png', '123456', '', 'C:\\Users\\hamza\\Documents\\SkillSwap\\uploads\\covers\\cover_10_e7044e6f-74de-44ed-a203-b5b473c7e1a7.png', '', 'entrepreneur', 'actif', NULL),
(11, 'hamza', 'najjar', 'hamzanajjar273@gmail.com', '555555', '+216 555789456', '/uploads/utilisateur/u_11_d3083e30-1788-479f-978f-bafab84886d0.jpeg', '123456', 'Mise en place d’un module permettant d’ajouter, modifier, rechercher et organiser les compétences des utilisateurs avec une interface simple, moderne et adaptée aux besoins professionnels.', 'C:\\Users\\hamza\\Documents\\SkillSwap\\uploads\\covers\\cover_11_de54cfc9-e4b4-4600-ba7e-37f8702c6d6c.png', 'tunis manouba', 'admin', 'actif', NULL),
(12, 'yassineaeeee', 'allala', 'yassine.alla@gmail.com', '555555', '+216 47589632', '/view/image/utilisateur/uploads/u_12_14699355-466d-49a6-bf87-1b86ea9f1487.png', '123456', 'Entrepreneur passionné par l’innovation, la création de projets et le développement d’activités, je suis motivé par les défis et la recherche de solutions concrètes. Sérieux, ambitieux et orienté résultats, je souhaite construire des projets durables, développer mon réseau professionnel et collaborer avec des personnes motivées partageant la même vision.', 'C:\\Users\\hamza\\Documents\\SkillSwap\\uploads\\covers\\cover_12_4f73d6b7-7d48-44a2-8a57-26d20e9754bf.png', 'tunis manouba', 'entrepreneur', 'actif', NULL),
(25, 'aymen', 'ayari', 'aymenayri@gmail.com', '555555', '57896400', NULL, '123456', NULL, NULL, NULL, 'entrepreneur', 'actif', '2026-04-15 19:14:06'),
(26, 'sallemi', 'nour', 'salleminour586@gmail.com', '555555', '+216 7896445341', '/view/image/utilisateur/uploads/u_26_dfca5aa2-b827-4faf-af6a-9de8ab78a1ac.png', '123456', '', NULL, '', 'freelance', 'actif', NULL),
(27, 'gaith', 'welhezi', 'gaith.welhezi.9@gmail.com', '555555', '+216 24563125', '/view/image/utilisateur/uploads/u_27_f43bc380-e254-428a-8ad4-14ae1192e5c9.png', '123456', '', 'C:\\Users\\hamza\\Documents\\SkillSwap\\uploads\\covers\\cover_27_02319da5-b497-44cf-b98d-57e5550f0316.png', '', 'freelance', 'actif', NULL),
(28, 'nourameni', 'nour', 'ameni.rommene@esprit.tn', '555555', '58411377', NULL, '123456', '', NULL, '', 'freelance', 'actif', '2026-05-12 15:44:56'),
(30, 'mehdi', 'bessrour', 'bessrour.mehdi@gmail.com', '555555', '+216 555469874', '/view/image/utilisateur/uploads/u_30_e9b5122d-1713-4ebd-a3bc-59fc6c0f1394.png', '123456', '', 'C:\\Users\\hamza\\Documents\\SkillSwap\\uploads\\covers\\cover_30_8b2fd83d-9a25-4f09-920a-4754e76c4001.png', '', 'entrepreneur', 'actif', NULL),
(31, 'hamza', 'jlassi', 'Hamza.Jlassi@esprit.tn', '555555', '+216 587965423', '/view/image/utilisateur/uploads/u_31_e3245160-b65b-4cc3-8b66-abf93a4adcfe.png', '123456', '', 'C:\\Users\\hamza\\Documents\\SkillSwap\\uploads\\covers\\cover_31_d6518d82-6261-4f4f-b530-469cf3243d63.png', '', 'freelance', 'actif', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `utilisateur_competence`
--

CREATE TABLE `utilisateur_competence` (
  `id_utilisateur` int(11) NOT NULL,
  `id_competence` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `banque`
--
ALTER TABLE `banque`
  ADD PRIMARY KEY (`id_banque`),
  ADD KEY `id_offre` (`id_offre`);

--
-- Index pour la table `candidature`
--
ALTER TABLE `candidature`
  ADD PRIMARY KEY (`id_candidature`),
  ADD KEY `id_utilisateur` (`id_utilisateur`),
  ADD KEY `id_offre` (`id_offre`);

--
-- Index pour la table `commentaire`
--
ALTER TABLE `commentaire`
  ADD PRIMARY KEY (`id_commentaire`),
  ADD KEY `id_offre` (`id_offre`),
  ADD KEY `id_utilisateur` (`id_utilisateur`);

--
-- Index pour la table `competences`
--
ALTER TABLE `competences`
  ADD PRIMARY KEY (`id`),
  ADD KEY `email` (`email`);

--
-- Index pour la table `connexion`
--
ALTER TABLE `connexion`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_demandeur` (`id_demandeur`),
  ADD KEY `id_receveur` (`id_receveur`);

--
-- Index pour la table `historiques_competences`
--
ALTER TABLE `historiques_competences`
  ADD PRIMARY KEY (`id`),
  ADD KEY `competence_id` (`competence_id`);

--
-- Index pour la table `message`
--
ALTER TABLE `message`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_envoyeur` (`id_envoyeur`),
  ADD KEY `id_receveur` (`id_receveur`);

--
-- Index pour la table `offre`
--
ALTER TABLE `offre`
  ADD PRIMARY KEY (`id_offre`),
  ADD UNIQUE KEY `matricule` (`matricule`),
  ADD KEY `id_utilisateur` (`id_utilisateur`);

--
-- Index pour la table `offre_like`
--
ALTER TABLE `offre_like`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_offre_user` (`id_offre`,`id_utilisateur`),
  ADD KEY `id_offre` (`id_offre`),
  ADD KEY `id_utilisateur` (`id_utilisateur`);

--
-- Index pour la table `postulation`
--
ALTER TABLE `postulation`
  ADD PRIMARY KEY (`id_postulation`),
  ADD KEY `id_rendez_vous` (`id_rendez_vous`),
  ADD KEY `id_postulant` (`id_postulant`);

--
-- Index pour la table `projet`
--
ALTER TABLE `projet`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Index pour la table `rendez_vous`
--
ALTER TABLE `rendez_vous`
  ADD PRIMARY KEY (`id_rendez_vous`),
  ADD KEY `id_admin_createur` (`id_admin_createur`);

--
-- Index pour la table `tache`
--
ALTER TABLE `tache`
  ADD PRIMARY KEY (`id`),
  ADD KEY `projet_id` (`projet_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Index pour la table `utilisateur`
--
ALTER TABLE `utilisateur`
  ADD PRIMARY KEY (`id_utilisateur`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Index pour la table `utilisateur_competence`
--
ALTER TABLE `utilisateur_competence`
  ADD PRIMARY KEY (`id_utilisateur`,`id_competence`),
  ADD KEY `id_competence` (`id_competence`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `banque`
--
ALTER TABLE `banque`
  MODIFY `id_banque` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `candidature`
--
ALTER TABLE `candidature`
  MODIFY `id_candidature` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `commentaire`
--
ALTER TABLE `commentaire`
  MODIFY `id_commentaire` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `competences`
--
ALTER TABLE `competences`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT pour la table `connexion`
--
ALTER TABLE `connexion`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT pour la table `historiques_competences`
--
ALTER TABLE `historiques_competences`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT pour la table `message`
--
ALTER TABLE `message`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT pour la table `offre`
--
ALTER TABLE `offre`
  MODIFY `id_offre` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `offre_like`
--
ALTER TABLE `offre_like`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `postulation`
--
ALTER TABLE `postulation`
  MODIFY `id_postulation` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `projet`
--
ALTER TABLE `projet`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `rendez_vous`
--
ALTER TABLE `rendez_vous`
  MODIFY `id_rendez_vous` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT pour la table `tache`
--
ALTER TABLE `tache`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `utilisateur`
--
ALTER TABLE `utilisateur`
  MODIFY `id_utilisateur` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `banque`
--
ALTER TABLE `banque`
  ADD CONSTRAINT `fk_banque_offre` FOREIGN KEY (`id_offre`) REFERENCES `offre` (`id_offre`) ON DELETE CASCADE;

--
-- Contraintes pour la table `candidature`
--
ALTER TABLE `candidature`
  ADD CONSTRAINT `candidature_ibfk_1` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE,
  ADD CONSTRAINT `candidature_ibfk_2` FOREIGN KEY (`id_offre`) REFERENCES `offre` (`id_offre`) ON DELETE CASCADE;

--
-- Contraintes pour la table `commentaire`
--
ALTER TABLE `commentaire`
  ADD CONSTRAINT `commentaire_ibfk_1` FOREIGN KEY (`id_offre`) REFERENCES `offre` (`id_offre`) ON DELETE CASCADE,
  ADD CONSTRAINT `commentaire_ibfk_2` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `competences`
--
ALTER TABLE `competences`
  ADD CONSTRAINT `competences_ibfk_1` FOREIGN KEY (`email`) REFERENCES `utilisateur` (`email`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Contraintes pour la table `connexion`
--
ALTER TABLE `connexion`
  ADD CONSTRAINT `connexion_ibfk_1` FOREIGN KEY (`id_demandeur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE,
  ADD CONSTRAINT `connexion_ibfk_2` FOREIGN KEY (`id_receveur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `historiques_competences`
--
ALTER TABLE `historiques_competences`
  ADD CONSTRAINT `historiques_competences_ibfk_1` FOREIGN KEY (`competence_id`) REFERENCES `competences` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `message`
--
ALTER TABLE `message`
  ADD CONSTRAINT `message_ibfk_1` FOREIGN KEY (`id_envoyeur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE,
  ADD CONSTRAINT `message_ibfk_2` FOREIGN KEY (`id_receveur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `offre`
--
ALTER TABLE `offre`
  ADD CONSTRAINT `offre_ibfk_1` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `offre_like`
--
ALTER TABLE `offre_like`
  ADD CONSTRAINT `fk_offre_like_offre` FOREIGN KEY (`id_offre`) REFERENCES `offre` (`id_offre`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_offre_like_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `postulation`
--
ALTER TABLE `postulation`
  ADD CONSTRAINT `postulation_ibfk_1` FOREIGN KEY (`id_rendez_vous`) REFERENCES `rendez_vous` (`id_rendez_vous`) ON DELETE CASCADE,
  ADD CONSTRAINT `postulation_ibfk_2` FOREIGN KEY (`id_postulant`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `projet`
--
ALTER TABLE `projet`
  ADD CONSTRAINT `projet_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `rendez_vous`
--
ALTER TABLE `rendez_vous`
  ADD CONSTRAINT `rendez_vous_ibfk_1` FOREIGN KEY (`id_admin_createur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `tache`
--
ALTER TABLE `tache`
  ADD CONSTRAINT `tache_ibfk_1` FOREIGN KEY (`projet_id`) REFERENCES `projet` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `tache_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `utilisateur_competence`
--
ALTER TABLE `utilisateur_competence`
  ADD CONSTRAINT `utilisateur_competence_ibfk_1` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE,
  ADD CONSTRAINT `utilisateur_competence_ibfk_2` FOREIGN KEY (`id_competence`) REFERENCES `competences` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
