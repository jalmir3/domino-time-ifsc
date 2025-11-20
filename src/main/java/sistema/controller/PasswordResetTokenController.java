package sistema.controller;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sistema.dto.NewPasswordDTO;
import sistema.service.UserService;

@Controller
@Data
@RequestMapping("/reset-password")
public class PasswordResetTokenController {
    private final UserService userService;

    @GetMapping
    public String showResetPasswordForm(@RequestParam("token") String token, RedirectAttributes redirectAttributes, Model model) {
        if (userService.isPasswordResetTokenValid(token)) {
            model.addAttribute("token", token);
            model.addAttribute("password", new NewPasswordDTO());
            return "reset-password";
        }
        redirectAttributes.addFlashAttribute("error", "Token inválido ou expirado. Por favor, solicite um novo link.");
        return "redirect:/forgot-password";
    }

    @PostMapping
    public String processResetPassword(@Valid @ModelAttribute("password") NewPasswordDTO passwordDto,
                                       BindingResult result,
                                       @RequestParam("token") String token,
                                       Model model) {
        try {
            passwordDto.validatePasswordMatch();
            if (result.hasErrors()) {
                model.addAttribute("token", token);
                return "reset-password";
            }
            userService.resetPassword(token, passwordDto.getNewPassword());
            model.addAttribute("message", "Senha alterada com sucesso!");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("confirmPassword", null, e.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        }
    }
}
