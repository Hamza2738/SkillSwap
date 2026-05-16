# 🚀 SkillSwap — Application javafx

## 🧩 Fonctionnalités principales

### ✅ Gestion des utilisateurs
- Inscription et connexion
- Création et modification du profil
- Gestion du compte (CRUD)

### ✅ Gestion des compétences
- Ajout et modification de compétences
- Catégorisation (IT, Design, Marketing, Business…)
- Recherche et filtrage

### ✅ Gestion des Offres
- Publier une offre (côté recruteur) afin d’attirer des candidats qualifiés
- Rechercher des offres par mots-clés (côté candidat) afin de trouver des postes adaptés
- Postuler à une offre afin de soumettre une candidature
- Consulter les candidatures reçues (côté recruteur) afin de sélectionner les profils pertinents

### ✅ Gestion des tâches
- Visualiser l’avancement global des tâches
- Définir une priorité pour chaque tâche afin de mieux organiser le travail
- Consulter l’historique des changements de statut d’une tâche
- Joindre des fichiers à une tâche pour partager des ressources
- Ajouter des commentaires sur une tâche pour communiquer avec les partenaires


---

## 🏗️ Architecture MVC
L’application suit une architecture **MVC** :

📦 SkillSwap Desktop

┣ 📂 model        → Modèles et classes métiers

┣ 📂 service      → Logique métier et traitement des données

┣ 📂 controller   → Contrôleurs JavaFX

┣ 📂 resources

┃  ┣ 📂 fxml       → Interfaces utilisateur JavaFX

┃  ┣ 📂 css        → Styles de l’application

┃  ┣ 📂 image      → Images et ressources graphiques

┃  ┗ 📂 files      → Fichiers utilisés par l’application

┣ 📂 utils        → Configuration, session et outils

┗ 📄 pom.xml      → Configuration Maven
