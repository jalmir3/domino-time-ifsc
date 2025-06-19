package sistema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sistema.model.GameGroup;
import sistema.model.GroupType;
import sistema.model.Match;
import sistema.model.User;
import sistema.service.GameGroupService;
import sistema.service.GroupService;
import sistema.service.MatchService;
import sistema.service.UserService;

import java.nio.file.AccessDeniedException;

@Controller
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GameGroupService gameGroupService;
    private final GroupService groupService;
    private final UserService userService;
    private final MatchService matchService;

    @GetMapping("/create")
    public String showCreateForm() {
        return "create-group";
    }

    @PostMapping
    public String createGroup(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("name") String name,
            @RequestParam(name = "groupType", defaultValue = "PUBLIC") GroupType groupType,
            RedirectAttributes redirectAttributes) {

        try {
            User creator = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            GameGroup group = gameGroupService.createGroup(creator, name, groupType);
            redirectAttributes.addFlashAttribute("success", "Grupo criado com sucesso!");
            return "redirect:/groups/" + group.getAccessCode();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao criar grupo: " + e.getMessage());
            return "redirect:/groups/create";
        }
    }

    @GetMapping("/{accessCode}")
    public String viewGroup(
            @PathVariable("accessCode") String accessCode,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User currentUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        GameGroup group = groupService.findByAccessCode(accessCode);
        boolean isCreator = group.getCreatedBy().getId().equals(currentUser.getId());

        model.addAttribute("group", group);
        model.addAttribute("isCreator", isCreator);
        model.addAttribute("players", gameGroupService.getPlayersInGroup(group.getId()));
        model.addAttribute("activeMatches", gameGroupService.getActiveMatches(group.getId()));

        return "access-code";
    }

    @PostMapping("/{accessCode}/start-match")
    public String startMatch(
            @PathVariable("accessCode") String accessCode,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) throws AccessDeniedException {

        User creator = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        GameGroup group = groupService.findByAccessCode(accessCode);

        if (!group.getCreatedBy().getId().equals(creator.getId())) {
            throw new AccessDeniedException("Apenas o criador pode iniciar partidas");
        }

        Match newMatch = matchService.startNewMatch(group, creator);
        redirectAttributes.addFlashAttribute("success", "Partida iniciada com sucesso!");

        return "redirect:/matches/" + newMatch.getId() + "/score";
    }

    @GetMapping("/join")
    public String showJoinForm() {
        return "join-game";
    }

    @PostMapping("/join")
    public String handleJoin(
            @RequestParam("accessCode") String accessCode,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            gameGroupService.addPlayerToGroup(user.getId(), accessCode);
            redirectAttributes.addFlashAttribute("success", "Você entrou no grupo com sucesso!");
            return "redirect:/groups/" + accessCode;

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocorreu um erro ao entrar na partida");
        }

        redirectAttributes.addFlashAttribute("accessCode", accessCode);
        return "redirect:/groups/join";
    }
}