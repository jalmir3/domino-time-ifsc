package sistema.controller;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sistema.dto.PasswordResetRequestDTO;
import sistema.service.EmailService;
import sistema.service.UserService;

@Controller
@Data
@RequestMapping("/forgot-password")
public class PasswordResetController {
    private final UserService userService;
    private final EmailService emailService;

    @GetMapping
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("request", new PasswordResetRequestDTO());
        return "forgot-password";
    }

    @PostMapping
    public String processForgotPassword(@Valid @ModelAttribute("request") PasswordResetRequestDTO request,
                                        BindingResult result,
                                        Model model) {
        if (result.hasErrors()) {
            return "forgot-password";
        }
        try {
            String token = userService.createPasswordResetToken(request.getEmail());
            String resetLink = "http://localhost:8080/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(request.getEmail(), resetLink);
            model.addAttribute("message", "Enviamos um link para redefinir sua senha para o email informado");
            return "forgot-password";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "forgot-password";
        }
    }
}