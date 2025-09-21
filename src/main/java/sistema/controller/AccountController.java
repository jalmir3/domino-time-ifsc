package sistema.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sistema.model.User;
import sistema.security.CustomUserDetails;
import sistema.service.UserService;

import java.security.Principal;

@Controller
@RequestMapping("/account")
public class AccountController {
    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
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
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            @RequestParam(value = "avatarBase64", required = false) String avatarBase64,
            Principal principal,
            Model model) {
        try {
            User user = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
            if (userService.isNicknameInUseByOtherUser(nickname, user.getId())) {
                model.addAttribute("errorMessage", "Este apelido já está em uso por outro usuário");
                model.addAttribute("user", user);
                return "account";
            }
            user.setNickname(nickname);
            user.setEmail(email);
            user.setBirthDate(java.time.LocalDate.parse(birthDate));
            if (avatarBase64 != null && !avatarBase64.isEmpty() && avatarBase64.startsWith("data:image")) {
                user.setAvatar(avatarBase64);
            }
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (!userService.validatePassword(user, currentPassword)) {
                    model.addAttribute("errorMessage", "Senha atual incorreta");
                    model.addAttribute("user", user);
                    return "account";
                }
                if (!newPassword.equals(confirmPassword)) {
                    model.addAttribute("errorMessage", "A nova senha e a confirmação não coincidem");
                    model.addAttribute("user", user);
                    return "account";
                }
                user.setPassword(userService.encodePassword(newPassword));
            }
            userService.updateUser(user);
            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            model.addAttribute("successMessage", "Conta atualizada com sucesso!");
            model.addAttribute("user", user);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao atualizar conta: " + e.getMessage());
            User user = userService.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("user", user);
            }
        }
        return "account";
    }

    @PostMapping("/delete")
    public String deleteAccount(
            @RequestParam("password") String password,
            Principal principal,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            User user = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
            if (!userService.validatePassword(user, password)) {
                model.addAttribute("errorMessage", "Senha incorreta");
                model.addAttribute("user", user);
                return "account";
            }
            userService.softDeleteUser(user.getId(), password);
            new SecurityContextLogoutHandler().logout(request, response,
                    SecurityContextHolder.getContext().getAuthentication());
            return "redirect:/login?deleted";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao desativar conta: " + e.getMessage());
            User user = userService.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("user", user);
            }
            return "account";
        }
    }
}