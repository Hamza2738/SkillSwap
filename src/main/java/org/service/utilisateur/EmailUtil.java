        package org.service.utilisateur;

        import jakarta.mail.*;
        import jakarta.mail.internet.InternetAddress;
        import jakarta.mail.internet.MimeMessage;

        import java.util.Properties;

        public final class EmailUtil {

            // ⚠️ Idéalement à mettre dans un fichier config plus tard
            private static final String SMTP_USER = "skillswapskillswap@gmail.com";
            private static final String SMTP_PASS = "rhmq cwok iwsj jbru";

            private EmailUtil() {
                // empêche l'instanciation
            }

            public static void sendOtp(String toEmail, String otpCode) throws MessagingException {

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SMTP_USER));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(toEmail));
                message.setSubject("SkillSwap - Code de vérification");

                message.setText(
                        "Bonjour,\n\n" +
                                "Voici votre code de vérification pour réinitialiser votre mot de passe : "
                                + otpCode + "\n\n" +
                                "Ce code expire dans 5 minutes.\n\n" +
                                "Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.\n\n" +
                                "SkillSwap"
                );

                Transport.send(message);
            }
        }