<?php

namespace App\Service\competence;

use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Address;
use Symfony\Component\Mime\Email;

class MailService
{
    public function __construct(private MailerInterface $mailer)
    {
    }

    public function sendSelectionEmail(string $toEmail, string $teamType): void
    {
        $subject = 'SkillSwap - Dossier accepté';

        $body =
            "Salut,\n\n" .
            "Nous avons le plaisir de vous informer que votre candidature a été acceptée et que vous êtes désormais validé au sein de notre équipe dans le domaine \"" . $teamType . "\".\n\n" .
            "Nous sommes ravis de vous compter parmi nous et sommes convaincus que vos compétences contribueront positivement à notre équipe.\n\n" .
            "Bienvenue dans l’équipe et plein succès dans cette nouvelle collaboration.\n\n" .
            "Cordialement,\n" .
            "L’équipe SkillSwap";

        $email = (new Email())
            ->from(new Address($_ENV['MAILER_FROM'] ?? 'skillswapskillswap@gmail.com', 'SkillSwap'))
            ->to($toEmail)
            ->subject($subject)
            ->text($body);

        $this->mailer->send($email);
    }

    public function sendExpiredEmail(string $toEmail, string $teamType): void
    {
        $subject = 'SkillSwap - Dossier refusé';

        $body =
            "Salut,\n\n" .
            "Nous vous remercions pour l’intérêt que vous portez à la plateforme SkillSwap et pour la soumission de votre compétence dans le domaine \"" . $teamType . "\".\n\n" .
            "Après étude de votre proposition, nous vous informons que celle-ci ne correspond pas aux besoins actuels.\n\n" .
            "Nous vous remercions néanmoins pour votre démarche et vous encourageons à consulter régulièrement nos mises à jour ou à proposer d’autres compétences susceptibles de mieux correspondre à nos critères.\n\n" .
            "Nous vous souhaitons pleine réussite dans vos projets.\n\n" .
            "Cordialement,\n" .
            "L’équipe SkillSwap";

        $email = (new Email())
            ->from(new Address($_ENV['MAILER_FROM'] ?? 'skillswapskillswap@gmail.com', 'SkillSwap'))
            ->to($toEmail)
            ->subject($subject)
            ->text($body);

        $this->mailer->send($email);
    }
}