<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class GeminiService
{
    public function __construct(
        private HttpClientInterface $client
    ) {
    }

    /** @param array<mixed> $profiles */
    public function ask(string $message, array $profiles = []): string
    {
        $apiKey = $_ENV['GEMINI_API_KEY'] ?? '';
        $model = $_ENV['GEMINI_MODEL'] ?? 'gemini-3-flash-preview';

        if ($apiKey === '') {
            return "Clé Gemini manquante. Ajoutez GEMINI_API_KEY dans .env.local";
        }

        $context = json_encode(
            $profiles,
            JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES
        );

        $prompt = <<<PROMPT
Tu es l'assistant SkillSwap.

Comprends la demande de l'utilisateur même avec fautes d'orthographe.
Cherche les profils les plus proches dans les données fournies.
Réponds en français simple et naturel.

Pour chaque profil trouvé, affiche si possible :
- email
- type
- catégorie
- niveau
- statut
- description

Si aucun profil ne correspond, explique-le et propose des mots-clés utiles.

Profils disponibles :
$context

Demande utilisateur :
$message
PROMPT;

        try {
            $response = $this->client->request(
                'POST',
                "https://generativelanguage.googleapis.com/v1beta/models/{$model}:generateContent",
                [
                    'headers' => [
                        'Content-Type' => 'application/json',
                        'x-goog-api-key' => $apiKey,
                    ],
                    'json' => [
                        'contents' => [
                            [
                                'parts' => [
                                    ['text' => $prompt]
                                ]
                            ]
                        ]
                    ],
                    'timeout' => 30,
                ]
            );

            $data = $response->toArray(false);

            if (!empty($data['error']['message'])) {
                return 'Erreur Gemini : ' . $data['error']['message'];
            }

            return $data['candidates'][0]['content']['parts'][0]['text']
                ?? 'Aucune réponse générée.';
        } catch (\Throwable $e) {
            return 'Erreur Gemini : ' . $e->getMessage();
        }
    }
}