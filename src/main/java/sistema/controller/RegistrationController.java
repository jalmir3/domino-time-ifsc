package sistema.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
                               Model model) {
        if (result.hasErrors()) {
            return registration;
        }
        try {
            userService.registerUserWithActivation(userDto);
            model.addAttribute("cadastroSucesso", true);
            model.addAttribute("email", userDto.getEmail());
            return registration;
        } catch (IllegalArgumentException e) {
            result.reject("erro.cadastro", e.getMessage());
            return registration;
        } catch (MessagingException e) {
            result.reject("erro.email", "Erro ao enviar e-mail de ativação");
            return registration;
        }
    }
}
