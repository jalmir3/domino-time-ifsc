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
import sistema.model.User;
import sistema.service.*;

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
            @PathVariable("accessCode") String accessCode,  // Nome explícito do parâmetro
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        GameGroup group = groupService.findByAccessCode(accessCode);

        model.addAttribute("group", group);
        return "access-code";
    }

    @PostMapping("/join")
    public String joinGroup(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String accessCode
    ) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        groupService.addPlayerToGroup(user, accessCode);
        return "redirect:/groups/" + accessCode;
    }

    @PostMapping("/{groupCode}/matches")
    public String startMatch(
            @PathVariable String groupCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        GameGroup group = groupService.findByAccessCode(groupCode);

        matchService.startMatch(group.getId(), user);
        return "redirect:/groups/" + groupCode;
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
