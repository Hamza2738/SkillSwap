package org.service.Competences.mailing;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class MailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;


    private static final String SMTP_USER = "skillswapskillswap@gmail.com";
    private static final String SMTP_PASS = "rhmq cwok iwsj jbru";

    private Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));

        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        props.put("mail.debug", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });
    }

    public void sendSelectionEmail(String toEmail, String teamType) throws MessagingException {
        Session session = createSession();

        String subject = "SkillSwap - Dossier accepté";
        String body =
                "Salut,\n\n" +
                        "Nous avons le plaisir de vous informer que votre candidature a été acceptée et que vous êtes désormais validé au sein de notre équipe dans le domaine \"" + teamType + "\".\n\n" +
                        "Nous sommes ravis de vous compter parmi nous et sommes convaincus que vos compétences contribueront positivement à notre équipe.\n\n" +
                        "Bienvenue dans l’équipe et plein succès dans cette nouvelle collaboration.\n\n" +
                        "Cordialement,\n" +
                        "L’équipe SkillSwap";

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setText(body, StandardCharsets.UTF_8.name());

        Transport.send(message);
    }


    public void sendExpiredEmail(String toEmail, String teamType) throws MessagingException {
        Session session = createSession();

        String subject = "SkillSwap - Dossier refusé";
        String body =
                "Salut,\n\n" +
                        "Nous vous remercions pour l’intérêt que vous portez à la plateforme SkillSwap et pour la soumission de votre compétence dans le domaine \"" + teamType + "\".\n\n" +
                        "Après étude de votre proposition, nous vous informons que celle-ci ne correspond pas aux besoins actuels.\n\n" +
                        "Nous vous remercions néanmoins pour votre démarche et vous encourageons à consulter régulièrement nos mises à jour ou à proposer d’autres compétences susceptibles de mieux correspondre à nos critères.\n\n" +
                        "Nous vous souhaitons pleine réussite dans vos projets.\n\n" +
                        "Cordialement,\n" +
                        "L’équipe SkillSwap";


        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setText(body, StandardCharsets.UTF_8.name());

        Transport.send(message);
    }
}
