<?php

namespace App\Controller\utilisateur;

use App\Entity\utilisateur\Utilisateur;
use App\Form\InscriptionType;
use App\Repository\UtilisateurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Annotation\Route;

class InscriptionController extends AbstractController
{
    private array $clesAutorisees = ["123456", "789456", "78963"];
    private string $cleDefautNonAdmin = "123456";

   #[Route('/inscription', name: 'inscription')]
public function inscription(
    Request $request,
    EntityManagerInterface $em,
    UtilisateurRepository $repo
): Response {
    $user = new Utilisateur();
    $form = $this->createForm(InscriptionType::class, $user);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        $data = $form->getData();
        $role = $data->getRole();

        $cle = $data->getCleAcces() ?? '';
        $isAdmin = strtolower($role) === 'admin';

        if ($isAdmin) {
            if (empty($cle)) {
                $this->addFlash('error', "Veuillez saisir la clé d'accès");
                return $this->redirectToRoute('inscription');
            }
            if (!in_array($cle, $this->clesAutorisees, true)) {
                $this->addFlash('error', "Clé d'accès invalide !");
                return $this->redirectToRoute('inscription');
            }
        } else {
            if (empty($cle) || !in_array($cle, $this->clesAutorisees, true)) {
                $user->setCleAcces($this->cleDefautNonAdmin);
            }
        }

        if ($repo->findOneBy(['email' => $data->getEmail()])) {
            $this->addFlash('error', 'Cet email est déjà utilisé.');
            return $this->redirectToRoute('inscription');
        }

        $motDePasse = $form->get('motDePasse')->getData();
        $confirm = $form->get('confirmMotDePasse')->getData();

        if ($motDePasse !== $confirm) {
            $this->addFlash('error', 'Les mots de passe ne correspondent pas.');
            return $this->redirectToRoute('inscription');
        }

        // SANS HASH
        $user->setMotDePasse($motDePasse);

        $em->persist($user);
        $em->flush();

        $this->addFlash('success', 'Inscription réussie ! Connectez-vous.');
        return $this->redirectToRoute('app_login');
    }

    return $this->render('utilisateur/inscription.html.twig', [
        'form' => $form->createView(),
    ]);
}
}