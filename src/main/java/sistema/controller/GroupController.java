package sistema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.webjars.NotFoundException;
import sistema.model.GameGroup;
import sistema.model.GameMode;
import sistema.model.Match;
import sistema.model.User;
import sistema.service.GameGroupService;
import sistema.service.GroupService;
import sistema.service.MatchService;
import sistema.service.UserService;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

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

    @GetMapping("/join")
    public String showJoinForm() {
        return "join-game";
    }

    @PostMapping
    public String createGroup(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("name") String name,
            RedirectAttributes redirectAttributes) {
        try {
            User creator = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
            GameGroup group = gameGroupService.createGroup(creator, name);
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
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        GameGroup group = groupService.findByAccessCode(accessCode);
        boolean isCreator = group.getCreatedBy().getId().equals(currentUser.getId());
        model.addAttribute("group", group);
        model.addAttribute("isCreator", isCreator);
        model.addAttribute("players", gameGroupService.getPlayersInGroup(group.getId()));
        model.addAttribute("activeMatches", gameGroupService.getActiveMatches(group.getId()));
        return "access-code";
    }

    @PostMapping("/{accessCode}/start")
    public String startMatch(
            @PathVariable String accessCode,
            @RequestParam GameMode gameMode,
            @RequestParam(required = false) List<UUID> teamA,
            @RequestParam(required = false) List<UUID> teamB,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        User creator = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        GameGroup group = groupService.findByAccessCode(accessCode);
        try {
            Match match = matchService.startConfiguredMatch(
                    group,
                    creator,
                    gameMode,
                    teamA,
                    teamB);
            redirectAttributes.addFlashAttribute("success", "Partida iniciada com sucesso!");
            return "redirect:/matches/" + match.getId() + "/score";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/groups/" + accessCode + "/configure";
        }
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
            e.printStackTrace();
        }
        redirectAttributes.addFlashAttribute("accessCode", accessCode);
        return "redirect:/groups/join";
    }

    @GetMapping("/{accessCode}/configure")
    public String configureMatch(
            @PathVariable("accessCode") String accessCode,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User currentUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        GameGroup group = groupService.findByAccessCode(accessCode);
        if (!group.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Apenas o criador pode configurar a partida");
        }
        List<User> players = gameGroupService.getPlayersInGroup(group.getId());
        model.addAttribute("group", group);
        model.addAttribute("players", players);
        model.addAttribute("maxPlayersReached", players.size() >= 4);
        model.addAttribute("gameModes", GameMode.values());
        return "configure-match";
    }

    @PostMapping("/{accessCode}/start-configured")
    public String startConfiguredMatch(
            @PathVariable("accessCode") String accessCode,
            @RequestParam("gameMode") GameMode gameMode,
            @RequestParam(value = "teamA", required = false) List<UUID> teamA,
            @RequestParam(value = "teamB", required = false) List<UUID> teamB,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User creator = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
            GameGroup group = groupService.findByAccessCode(accessCode);
            if (!group.getCreatedBy().getId().equals(creator.getId())) {
                throw new AccessDeniedException("Apenas o criador pode iniciar partidas");
            }
            List<User> players = gameGroupService.getPlayersInGroup(group.getId());
            if (GameMode.INDIVIDUAL.equals(gameMode)) {
                if (players.size() < 2) {
                    throw new IllegalStateException("O modo individual requer pelo menos 2 jogadores");
                }
            } else if (GameMode.TEAMS.equals(gameMode)) {
                if (players.size() != 4) {
                    throw new IllegalStateException("O modo em duplas requer exatamente 4 jogadores");
                }
                if (teamA == null || teamB == null || teamA.size() != 2 || teamB.size() != 2) {
                    throw new IllegalArgumentException("Selecione exatamente 2 jogadores para cada time");
                }
                Set<UUID> allSelected = new HashSet<>();
                allSelected.addAll(teamA);
                allSelected.addAll(teamB);
                Set<UUID> actualPlayers = players.stream()
                        .map(User::getId)
                        .collect(Collectors.toSet());
                if (!actualPlayers.containsAll(allSelected)) {
                    throw new IllegalArgumentException("Todos os jogadores selecionados devem estar no grupo");
                }
                gameGroupService.saveTeams(group.getId(), teamA, teamB);
            }
            Match newMatch = matchService.startConfiguredMatch(group, creator, gameMode, teamA, teamB);
            redirectAttributes.addFlashAttribute("success", "Partida iniciada com sucesso!");
            return "redirect:/matches/" + newMatch.getId() + "/score";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/groups/" + accessCode + "/configure";
        }
    }

    @GetMapping("/{accessCode}/status")
    @ResponseBody
    public Map<String, Object> checkGroupStatus(@PathVariable("accessCode") String accessCode) {
        GameGroup group = groupService.findByAccessCode(accessCode);
        List<User> players = gameGroupService.getPlayersInGroup(group.getId());
        return Map.of(
                "maxPlayersReached", players.size() >= 4,
                "playerCount", players.size()
        );
    }
}