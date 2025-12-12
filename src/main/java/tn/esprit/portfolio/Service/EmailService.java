package tn.esprit.portfolio.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.esprit.portfolio.Entity.ContactMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.contact.recipient-email}")
    private String recipientEmail;

    public void sendContactNotification(ContactMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(recipientEmail);
            helper.setFrom(message.getEmail(), message.getName());

            // Sujet professionnel
            helper.setSubject("Nouvelle demande de contact - Portfolio");

            helper.setText(buildEmailContent(message), true);
            helper.setReplyTo(message.getEmail());

            mailSender.send(mimeMessage);
            log.info("Email sent successfully from: {} <{}>", message.getName(), message.getEmail());
        } catch (MessagingException e) {
            log.error("Error sending email for contact message", e);
        } catch (UnsupportedEncodingException e) {

        }
    }

    private String buildEmailContent(ContactMessage message) {
        String currentDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.8;
                        color: #333;
                        background-color: #f9f9f9;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 650px;
                        margin: 30px auto;
                        background: #ffffff;
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.08);
                        overflow: hidden;
                    }
                    .content {
                        padding: 40px;
                    }
                    .message-text {
                        font-size: 16px;
                        line-height: 1.8;
                        color: #333;
                        white-space: pre-wrap;
                        word-wrap: break-word;
                        margin-bottom: 30px;
                    }
                    .divider {
                        height: 1px;
                        background: linear-gradient(to right, transparent, #e0e0e0, transparent);
                        margin: 30px 0;
                    }
                    .footer-info {
                        background: #f8f9fa;
                        padding: 20px;
                        border-radius: 6px;
                        font-size: 13px;
                        color: #666;
                    }
                    .footer-info p {
                        margin: 8px 0;
                    }
                    .footer-info strong {
                        color: #333;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="content">
                        <div class="message-text">%s</div>
                        
                        <div class="divider"></div>
                        
                        <div class="footer-info">
                            <p><strong>Envoyé par :</strong> %s</p>
                            <p><strong>Email :</strong> <a href="mailto:%s" style="color: #667eea; text-decoration: none;">%s</a></p>
                            <p><strong>Date :</strong> %s</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                message.getMessage(),
                message.getName(),
                message.getEmail(),
                message.getEmail(),
                currentDate
        );
    }
}