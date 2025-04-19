package sistema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
    public String activateAccount(@RequestParam("token") String token, Model model) {
        boolean activated = userService.activateUser(token);

        if (activated) {
            return "activation-success";
        } else {
            model.addAttribute("error", "Token de ativação inválido ou expirado");
            return "activation-error";
        }
    }

    @GetMapping("/success")
    public String showActivationSuccessPage() {
        return "activation-success";
    }
}