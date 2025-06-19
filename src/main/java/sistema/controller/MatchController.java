package sistema.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.webjars.NotFoundException;
import sistema.dto.ScoreDTO;
import sistema.model.Match;
import sistema.model.User;
import sistema.service.GameGroupService;
import sistema.service.MatchService;
import sistema.service.UserService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@Slf4j
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;
    private final UserService userService;
    private final GameGroupService gameGroupService;

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

    @PostMapping(value = "/{matchId}/save-scores",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> saveScores(
            @PathVariable("matchId") UUID matchId,
            @RequestParam("playerIds[]") List<UUID> playerIds,
            @RequestParam("scores[]") List<Integer> scores,
            Principal principal) {

        try {
            if (playerIds.size() != scores.size()) {
                throw new RuntimeException("Número de jogadores e pontuações não corresponde");
            }

            List<ScoreDTO> scoreDTOs = IntStream.range(0, playerIds.size())
                    .mapToObj(i -> new ScoreDTO(playerIds.get(i), scores.get(i)))
                    .collect(Collectors.toList());

            matchService.saveScores(matchId, scoreDTOs);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{matchId}/cancel")
    public String cancelMatch(
            @PathVariable("matchId") UUID matchId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            matchService.cancelMatch(matchId);
            redirectAttributes.addFlashAttribute("success", "Partida cancelada com sucesso!");

            return "redirect:/groups/create";

        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Partida não encontrada");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro inesperado ao cancelar partida");
            log.error("Erro ao cancelar partida", e);
        }

        return "redirect:/matches/" + matchId;
    }
}
