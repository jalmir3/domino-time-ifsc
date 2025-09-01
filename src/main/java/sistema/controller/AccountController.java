package sistema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sistema.model.User;
import sistema.service.FileStorageService;
import sistema.service.UserService;

import java.security.Principal;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public AccountController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String accountPage(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        model.addAttribute("user", user);
        return "account";
    }

    @PostMapping("/update")
    public String updateAccount(
            @RequestParam("nickname") String nickname,
            @RequestParam("email") String email,
            @RequestParam("birthDate") String birthDate,
            @RequestParam(value = "currentPassword", required = false) String currentPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            user.setNickname(nickname);
            user.setEmail(email);
            user.setBirthDate(java.time.LocalDate.parse(birthDate));

            if (avatar != null && !avatar.isEmpty()) {
                String avatarUrl = fileStorageService.storeFile(avatar, "avatars", user.getId().toString());
                user.setAvatarUrl(avatarUrl);
            }

            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (!userService.validatePassword(user, currentPassword)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Senha atual incorreta");
                    return "redirect:/account";
                }
                user.setPassword(userService.encodePassword(newPassword));
            }

            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Conta atualizada com sucesso!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar conta: " + e.getMessage());
        }

        return "redirect:/account";
    }

    @PostMapping("/delete")
    public String deleteAccount(
            @RequestParam("password") String password,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            if (!userService.validatePassword(user, password)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Senha incorreta");
                return "redirect:/account";
            }

            userService.deleteUser(user.getId());

            redirectAttributes.addFlashAttribute("successMessage", "Sua conta foi excluída com sucesso.");
            return "redirect:/logout";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao excluir conta: " + e.getMessage());
            return "redirect:/account";
        }
    }
}