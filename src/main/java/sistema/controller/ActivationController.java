package sistema.controller;

import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sistema.service.UserService;

@Controller
@Data
@RequestMapping("/activate")
public class ActivationController {

    private final UserService userService;

    @GetMapping
    public String activateAccount(@RequestParam("token") String token, Model model) {
        boolean activated = userService.activateUser(token);

        if (activated) {
            model.addAttribute("ativacaoSucesso", true);
            return "login";
        } else {
            model.addAttribute("ativacaoErro", true);
            model.addAttribute("error", "Token de ativação inválido ou expirado");
            return "login";
        }
    }

    @GetMapping("/success")
    public String showActivationSuccessPage() {
        return "activation-success";
    }
}