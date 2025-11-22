package sistema.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.util.List;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private TransactionalEmailsApi cachedApi = null;

    private TransactionalEmailsApi getApiInstance() {
        if (cachedApi == null) {
            cachedApi = new TransactionalEmailsApi();
            cachedApi.getApiClient().setApiKey(brevoApiKey);
        }
        return cachedApi;
    }

    public void sendActivationEmail(String to, String activationLink) throws Exception {
        TransactionalEmailsApi api = getApiInstance();

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
        TransactionalEmailsApi api = getApiInstance();

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
