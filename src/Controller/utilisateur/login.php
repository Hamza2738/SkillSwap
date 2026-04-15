<?php

namespace App\Controller\utilisateur;

use App\Entity\utilisateur\Utilisateur;
use App\Form\LoginType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;

class login extends AbstractController
{
    // ─── ACCUEIL ───────────────────────────────────────────
    #[Route('/accueil', name: 'app_accueil')]
    public function accueil(): Response
    {
        return $this->render('accueil/index.html.twig');
    }

    // ─── LOGIN ─────────────────────────────────────────────
    #[Route('/login', name: 'app_login')]
    public function login(AuthenticationUtils $authenticationUtils): Response
    {
        if ($this->getUser()) {
            return $this->redirectToRoute('app_dashboard');
        }

        $error        = $authenticationUtils->getLastAuthenticationError();
        $lastUsername = $authenticationUtils->getLastUsername();

        $form = $this->createForm(LoginType::class, [
            'email' => $lastUsername,
        ]);

        return $this->render('utilisateur/login.html.twig', [
            'loginForm'     => $form->createView(),
            'last_username' => $lastUsername,
            'error'         => $error,
        ]);
    }

    // ─── LOGOUT ────────────────────────────────────────────
    #[Route('/logout', name: 'app_logout')]
    public function logout(): void {}

    // ─── INSCRIPTION ───────────────────────────────────────
   
#[Route('/inscription', name: 'app_inscription', methods: ['GET', 'POST'])]
public function inscription(
    Request $request,
    EntityManagerInterface $em
): Response {
    if ($request->isMethod('POST')) {

        $utilisateur = new Utilisateur();

        $utilisateur->setNom($request->request->get('nom'));
        $utilisateur->setPrenom($request->request->get('prenom'));
        $utilisateur->setEmail($request->request->get('email'));
        $utilisateur->setTelephone($request->request->get('telephone'));
        $utilisateur->setRole($request->request->get('role', 'freelance'));
        $utilisateur->setStatut('actif');

        // ❌ SANS HASH (mot de passe direct)
        $utilisateur->setMotDePasse($request->request->get('mot_de_passe'));

        $em->persist($utilisateur);
        $em->flush();

        $this->addFlash('success', 'Compte créé avec succès.');
        return $this->redirectToRoute('app_login');
    }

    return $this->render('utilisateur/inscription.html.twig');
}

    // ─── DASHBOARD (cible après login) ─────────────────────
    #[Route('/dashboard', name: 'app_dashboard')]
    public function dashboard(): Response
    {
        $user = $this->getUser();

        if (!$user) {
            return $this->redirectToRoute('app_login');
        }

        return $this->redirectByRole();
    }

    
 
    // ─── BACK-OFFICE ADMIN ─────────────────────────────────
   #[Route('/admin/competences', name: 'app_admin_competences')]
public function adminCompetences(
    Request $request,
    \Doctrine\ORM\EntityManagerInterface $em
): Response {
    /** @var \App\Entity\utilisateur\Utilisateur $user */
    $user = $this->getUser();

    if (!$user || !in_array('ROLE_ADMIN', $user->getRoles(), true)) {
        return $this->redirectToRoute('app_login');
    }

    $competences = $em->getRepository(\App\Entity\competence\Competence::class)
        ->findAll();

    // ── Formulaire d'ajout pour l'admin
    $competence = new \App\Entity\competence\Competence();
    $form = $this->createForm(\App\Form\CompetenceType::class, $competence);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        $em->persist($competence);
        $em->flush();
        return $this->redirectToRoute('app_admin_competences');
    }

    return $this->render('Competences/back/competence.html.twig', [
        'user'        => $user,
        'competences' => $competences,
        'form'        => $form->createView(),       // ← manquait
        'formAction'  => $this->generateUrl('app_admin_competences'), // ← manquait
    ]);
}
  // ─── MOT DE PASSE OUBLIÉ ───────────────────────────────
 #[Route('/forgot-password', name: 'app_forgot_password', methods: ['GET', 'POST'])]
public function forgotPassword(
    Request $request,
    EntityManagerInterface $em,
    \Symfony\Component\Mailer\MailerInterface $mailer
): Response {
    if ($request->isMethod('GET')) {
        return $this->redirectToRoute('app_login');
    }

    $email = trim((string) $request->request->get('email', ''));

    if ($email === '') {
        return $this->json([
            'success' => false,
            'message' => 'Veuillez saisir votre adresse email.',
        ], 400);
    }

    /** @var \App\Entity\utilisateur\Utilisateur|null $user */
    $user = $em->getRepository(\App\Entity\utilisateur\Utilisateur::class)
        ->findOneBy(['email' => $email]);

    if (!$user) {
        return $this->json([
            'success' => false,
            'message' => 'Aucun compte trouvé avec cette adresse email.',
        ], 404);
    }

    try {
        $temporaryPassword = 'SS' . random_int(100000, 999999);

        // SANS HASH
        $user->setMotDePasse($temporaryPassword);
        $em->flush();

        $message = (new \Symfony\Component\Mime\Email())
            ->from(new \Symfony\Component\Mime\Address(
                $_ENV['MAILER_FROM'] ?? 'skillswapskillswap@gmail.com',
                'SkillSwap'
            ))
            ->to($email)
            ->subject('SkillSwap - Réinitialisation de votre mot de passe')
            ->html("
                <h2>Réinitialisation du mot de passe</h2>
                <p>Bonjour {$user->getPrenom()} {$user->getNom()},</p>
                <p>Votre mot de passe temporaire est :</p>
                <p style='font-size:22px;font-weight:bold;color:#4f46e5;'>{$temporaryPassword}</p>
                <p>Connectez-vous avec ce mot de passe puis changez-le rapidement.</p>
                <br>
                <p>— L’équipe SkillSwap</p>
            ");

        $mailer->send($message);

        return $this->json([
            'success' => true,
            'message' => 'Email envoyé avec succès.',
        ]);
    } catch (\Throwable $e) {
        return $this->json([
            'success' => false,
            'message' => 'Erreur lors de l’envoi de l’email : ' . $e->getMessage(),
        ], 500);
    }
}

   #[Route('/competences', name: 'app_front_competences')]
public function frontCompetences(
    Request $request,
    \Doctrine\ORM\EntityManagerInterface $em
): Response {
    /** @var \App\Entity\utilisateur\Utilisateur $user */
    $user = $this->getUser();

    if (!$user) {
        return $this->redirectToRoute('app_login');
    }

    // ── Récupère les compétences de l'utilisateur
    $competences = $em->getRepository(\App\Entity\competence\Competence::class)
        ->findBy(['email' => $user->getEmail()]);

    // ── Crée le formulaire pour ajouter/éditer une compétence
    $competence = new \App\Entity\competence\Competence();
    $form = $this->createForm(\App\Form\CompetenceType::class, $competence);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        $competence->setEmail($user->getEmail());
        // Si ton entité a idUtilisateur : $competence->setIdUtilisateur($user->getIdUtilisateur());
        $em->persist($competence);
        $em->flush();
        return $this->redirectToRoute('app_front_competences');
    }

   return $this->render('Competences/front/CompetenceFront.html.twig', [
    'user'        => $user,
    'competences' => $competences,
    'form'        => $form->createView(),
    'email'       => $user->getEmail(),
    'error'       => null,   // ← variable manquante
]);
}

  

    // ─── HELPER ────────────────────────────────────────────
private function redirectByRole(): Response
{
    /** @var \App\Entity\utilisateur\Utilisateur|null $user */
    $user = $this->getUser();

    if (!$user) {
        return $this->redirectToRoute('app_login');
    }

    $role = strtolower(trim((string) $user->getRole()));

    if ($role === 'admin') {
        return $this->redirectToRoute('app_admin_profil');
    }

    if (in_array($role, ['freelancer', 'entrepreneur', 'freelance'], true)) {
        return $this->redirectToRoute('app_profil');
    }

    return $this->redirectToRoute('app_profil');
}
}