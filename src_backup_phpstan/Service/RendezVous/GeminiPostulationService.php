<?php

namespace App\Service\RendezVous;

use App\Entity\RendezVous\Postulation;
use App\Entity\RendezVous\RendezVous;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class GeminiPostulationService
{
    private string $apiKey;
    private string $model;

    public function __construct(private HttpClientInterface $client)
    {
        $this->apiKey = $_ENV['GEMINI_API_KEY'] ?? '';
        $this->model = $_ENV['GEMINI_MODEL'] ?? 'gemini-1.5-flash';
    }

    public function analyserPostulation(RendezVous $rendezVous, Postulation $postulation): string
    {
        if (!$this->apiKey) {
            return 'Erreur : clé API Gemini manquante dans le fichier .env';
        }

        $prompt = $this->buildPrompt($rendezVous, $postulation);

        try {
            $response = $this->client->request(
                'POST',
                'https://generativelanguage.googleapis.com/v1beta/models/' . $this->model . ':generateContent?key=' . $this->apiKey,
                [
                    'json' => [
                        'contents' => [
                            [
                                'parts' => [
                                    [
                                        'text' => $prompt
                                    ]
                                ]
                            ]
                        ]
                    ]
                ]
            );

            $data = $response->toArray(false);

            return $data['candidates'][0]['content']['parts'][0]['text']
                ?? 'Gemini n’a pas retourné de réponse.';
        } catch (\Throwable $e) {
            return 'Erreur Gemini : ' . $e->getMessage();
        }
    }

    private function buildPrompt(RendezVous $rendezVous, Postulation $postulation): string
    {
        return "
Tu es un assistant intelligent pour une plateforme SkillSwap.

Analyse cette postulation selon le rendez-vous suivant.

RENDEZ-VOUS :
- Titre : " . ($rendezVous->getTitre() ?? 'Non défini') . "
- Description : " . ($rendezVous->getDescription() ?? 'Non définie') . "
- Compétence demandée : " . ($rendezVous->getCompetence() ?? 'Non définie') . "
- Type : " . ($rendezVous->getType() ?? 'Non défini') . "
- Nombre de places : " . ($rendezVous->getNombrePlaces() ?? 0) . "

POSTULANT :
- Nom : " . ($postulation->getNom() ?? 'Non défini') . "
- Prénom : " . ($postulation->getPrenom() ?? 'Non défini') . "
- Rôle : " . ($postulation->getRole() ?? 'Non défini') . "
- Titre du postulant : " . ($postulation->getTitre() ?? 'Non défini') . "
- Message : " . ($postulation->getMessage() ?? 'Aucun message') . "
- CV : " . ($postulation->getCv() ? 'CV fourni' : 'CV non fourni') . "
- Statut actuel : " . ($postulation->getStatus() ?? 'en_attente') . "

Donne une analyse courte et claire en français avec cette structure :

Score : /100
Avis :
Points forts :
Points faibles :
Recommandation : accepter / refuser / revoir manuellement

Ne dépasse pas 8 lignes.
";
    }
}