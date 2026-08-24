package com.studyflow.auth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class PasswordResetDeliveryService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetDeliveryService.class);
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final String frontendUrl;
    private final String from;

    public PasswordResetDeliveryService(ObjectProvider<JavaMailSender> mailSenders,
                                        @Value("${studyflow.frontend-url:http://localhost:3000}") String frontendUrl,
                                        @Value("${studyflow.security.password-reset-from:noreply@studyflow.local}") String from) {
        this.mailSenders = mailSenders;
        this.frontendUrl = frontendUrl;
        this.from = from;
    }

    public boolean deliver(String email, String token) {
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (sender == null) return false;
        String link = frontendUrl + "/?resetToken=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Recuperação de senha — StudyFlow");
        message.setText("Recebemos um pedido para redefinir sua senha.\n\n" + link
                + "\n\nO link expira em 30 minutos e só pode ser usado uma vez. Se você não fez este pedido, ignore esta mensagem.");
        try {
            sender.send(message);
            return true;
        } catch (MailException exception) {
            log.error("Não foi possível enviar o e-mail de recuperação.", exception);
            return false;
        }
    }
}
