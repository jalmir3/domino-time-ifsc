package sistema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sistema.model.GameGroup;
import sistema.model.Match;
import sistema.model.User;
import sistema.service.GameGroupService;
import sistema.service.GroupService;
import sistema.service.MatchService;
import sistema.service.UserService;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;
    private final UserService userService;
    private final GameGroupService gameGroupService;
    private final GroupService groupService;

    @GetMapping("/{matchId}/score")
    public String showScoreForm(
            @PathVariable("matchId") UUID matchId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User currentUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        Match match = matchService.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));

        if (!gameGroupService.isUserInGroup(currentUser.getId(), match.getGroup().getId())) {
            throw new AccessDeniedException("Acesso não autorizado à partida");
        }

        model.addAttribute("match", match);
        model.addAttribute("group", match.getGroup());
        model.addAttribute("players", gameGroupService.getPlayersInGroup(match.getGroup().getId()));
        model.addAttribute("isCreator",
                match.getGroup().getCreatedBy().getId().equals(currentUser.getId()));

        return "score-form";
    }

    @PostMapping("/{accessCode}/start-match")
    public String startMatch(
            @PathVariable("accessCode") String accessCode,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User creator = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        GameGroup group = groupService.findByAccessCode(accessCode);

        if (!gameGroupService.isUserInGroup(creator.getId(), group.getId())) {
            throw new AccessDeniedException("Você não é membro deste grupo");
        }

        Match newMatch = matchService.startNewMatch(group, creator);
        redirectAttributes.addFlashAttribute("success", "Partida iniciada com sucesso!");

        return "redirect:/matches/" + newMatch.getId() + "/score";
    }

    @PostMapping("/{matchId}/scores")
    public String saveScores(
            @PathVariable UUID matchId,
            @RequestParam Map<String, String> scores,
            @RequestParam(required = false) Map<String, String> winners,
            @AuthenticationPrincipal UserDetails userDetails) {

        User creator = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        matchService.saveMatchScores(matchId, creator, scores, winners != null ? winners.keySet() : Set.of());

        return "redirect:/matches/" + matchId;
    }
}
