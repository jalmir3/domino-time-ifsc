package sistema.service;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.util.List;

@Service
@AllArgsConstructor
public class EmailService {
//    private final JavaMailSender mailSender;
//
//    public void sendActivationEmail(String to, String activationLink) throws MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//        helper.setTo(to);
//        helper.setSubject("Ative sua conta - Clique no link");
//        helper.setText("<h3>Bem-vindo!</h3>"
//                        + "<p>Clique no link abaixo para ativar sua conta:</p>"
//                        + "<p><a href=\"" + activationLink + "\">Ative sua conta</a></p>"
//                        + "<p>Se você não solicitou este cadastro, por favor ignore este e-mail.</p>",
//                true);
//        mailSender.send(message);
//    }
//
//    public void sendPasswordResetEmail(String to, String resetLink) throws MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//        helper.setTo(to);
//        helper.setSubject("Redefinição de Senha");
//        helper.setText("<p>Clique no link abaixo para redefinir sua senha:</p>"
//                + "<p><a href=\"" + resetLink + "\">Redefinir Senha</a></p>"
//                + "<p>Se você não solicitou esta redefinição, por favor ignore este email.</p>", true);
//        mailSender.send(message);
//    }

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    public void sendActivationEmail(String to, String activationLink) throws Exception {

        TransactionalEmailsApi api = new TransactionalEmailsApi();
        api.getApiClient().setApiKey(brevoApiKey);

        SendSmtpEmail email = new SendSmtpEmail()
                .sender(new SendSmtpEmailSender()
                        .email(senderEmail)
                        .name(senderName))
                .to(List.of(new SendSmtpEmailTo().email(to)))
                .subject("Ative sua conta")
                .htmlContent(
                        "<h3>Bem-vindo!</h3>" +
                                "<p>Clique no link abaixo para ativar sua conta:</p>" +
                                "<a href='" + activationLink + "'>Ativar Conta</a>"
                );

        api.sendTransacEmail(email);
    }

    public void sendPasswordResetEmail(String to, String resetLink) throws Exception {

        TransactionalEmailsApi api = new TransactionalEmailsApi();
        api.getApiClient().setApiKey(brevoApiKey);

        SendSmtpEmail email = new SendSmtpEmail()
                .sender(new SendSmtpEmailSender()
                        .email(senderEmail)
                        .name(senderName))
                .to(List.of(new SendSmtpEmailTo().email(to)))
                .subject("Redefinição de Senha")
                .htmlContent(
                        "<p>Clique no link para redefinir sua senha:</p>" +
                                "<a href='" + resetLink + "'>Redefinir Senha</a>"
                );

        api.sendTransacEmail(email);
    }
}
