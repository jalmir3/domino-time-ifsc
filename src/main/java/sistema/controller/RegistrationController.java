package sistema.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sistema.dto.UserRegistrationDto;
import sistema.service.UserService;

@Controller
@RequestMapping("/register")
public class RegistrationController {
    private final UserService userService;

    private String registration = "registration";

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return registration;
    }

    @PostMapping
    public String registerUser(@ModelAttribute("user") @Valid UserRegistrationDto userDto,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        String email = "email";
        if (result.hasErrors()) {
            return registration;
        }
        try {
            userService.registerUserWithActivation(userDto);
            redirectAttributes.addAttribute(email, userDto.getEmail());
            return "redirect:/register/success";
        } catch (IllegalArgumentException e) {
            result.reject("erro.cadastro", e.getMessage());
            return registration;
        } catch (MessagingException e) {
            result.reject("erro.email", "Erro ao enviar e-mail de ativação");
            return registration;
        }
    }

    @GetMapping("/success")
    public String showSuccessPage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "registration-success";
    }
}
