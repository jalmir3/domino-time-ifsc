package sistema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sistema.service.UserService;

@Controller
@RequestMapping("/activate")
public class ActivationController {
    private final UserService userService;

    public ActivationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showActivationForm(
            @RequestParam(value = "email", required = false, defaultValue = "") String email,
            Model model) {
        model.addAttribute("email", email);
        return "activation";
    }

    @PostMapping
    public String activateUser(
            @RequestParam("email") String email,
            @RequestParam("pin") String pin,
            Model model) {

        boolean activated = userService.activateUser(email, pin);

        if (activated) {
            return "redirect:/activate/success";
        } else {
            model.addAttribute("error", "Código de ativação inválido");
            model.addAttribute("email", email);
            return "activation";
        }
    }

    @GetMapping("/success")
    public String showActivationSuccessPage() {
        return "activation-success";
    }
}