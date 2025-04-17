package sistema.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendActivationEmail(String to, String activationLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Ative sua conta - Clique no link");
        helper.setText("<h3>Bem-vindo!</h3>"
                        + "<p>Clique no link abaixo para ativar sua conta:</p>"
                        + "<p><a href=\"" + activationLink + "\">" + activationLink + "</a></p>"
                        + "<p>Se você não solicitou este cadastro, por favor ignore este e-mail.</p>",
                true);

        mailSender.send(message);
    }
}
