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
import sistema.service.*;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Controller
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GameGroupService gameGroupService;
    private final GroupService groupService;
    private final UserService userService;
    private final MatchService matchService;
    private final PlayerScoreService playerScoreService;

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        return "create-group";
    }

    @PostMapping
    public String createGroup(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("name") String name,
            @RequestParam(value = "groupType", defaultValue = "PUBLIC") GroupType groupType,
            RedirectAttributes redirectAttributes) {

        User creator = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        GameGroup group = gameGroupService.createGroup(creator, name, groupType);
        redirectAttributes.addFlashAttribute("success", "Grupo criado com sucesso!");
        return "redirect:/groups/" + group.getAccessCode();
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

        Match newMatch = matchService.startNewMatch(group,creator);
        redirectAttributes.addFlashAttribute("success", "Partida iniciada com sucesso!");

        return "redirect:/matches/" + newMatch.getId() + "/score";
    }

    @PostMapping("/matches/{matchId}/scores")
    public String registerScore(
            @PathVariable UUID matchId,
            @RequestParam UUID userId,
            @RequestParam int score,
            @RequestParam boolean isWinner
    ) {
        playerScoreService.registerScore(matchId, userId, score, isWinner);
        return "redirect:/matches/" + matchId;
    }
}