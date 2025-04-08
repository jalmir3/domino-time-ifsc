package sistema.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import sistema.dto.UserRegistrationDto;
import sistema.service.UserService;

@Controller
@RequestMapping("/register")
public class RegistrationController {
    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "registration";
    }

    @PostMapping
    public String registerUser(@ModelAttribute("user") @Valid UserRegistrationDto userDto,
                               BindingResult result) {
        if (result.hasErrors()) {
            return "registration";
        }

        try {
            userService.registerUser(userDto);
            return "redirect:/register/success?email=" + userDto.getEmail();
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", null, e.getMessage());
            return "registration";
        }
    }

    @GetMapping("/success")
    public String showSuccessPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "registration-success";
    }
}
