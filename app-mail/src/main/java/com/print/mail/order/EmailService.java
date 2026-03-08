package com.print.mail.order;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from:no-reply@printxl.com}")
    private String from;

    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Reinitialisation de votre mot de passe");

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetLink", resetLink);

            String html = templateEngine.process(
                    "emails/password-reset",
                    context
            );

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Erreur envoi mail reset password a {}", toEmail, e);
        }
    }
}
