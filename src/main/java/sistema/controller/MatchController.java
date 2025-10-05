package sistema.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import sistema.dto.MatchDetailsDTO;
import sistema.dto.PlayerMatchDTO;
import sistema.dto.ScoreDTO;
import sistema.model.*;
import sistema.service.GameGroupService;
import sistema.service.MatchService;
import sistema.service.PlayerScoreService;
import sistema.service.UserService;

import java.util.*;
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
    private final PlayerScoreService playerScoreService;

    @PostMapping(value = "/{matchId}/save-scores",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String saveScores(
            @PathVariable("matchId") UUID matchId,
            @RequestParam("playerIds[]") List<UUID> playerIds,
            @RequestParam("scores[]") List<Integer> scores,
            RedirectAttributes redirectAttributes) {
        try {
            if (playerIds.size() != scores.size()) {
                throw new RuntimeException("Número de jogadores e pontuações não corresponde");
            }
            List<ScoreDTO> scoreDTOs = IntStream.range(0, playerIds.size())
                    .mapToObj(i -> new ScoreDTO(playerIds.get(i), scores.get(i)))
                    .collect(Collectors.toList());
            UUID winnerId = playerScoreService.saveScoresAndGetWinner(matchId, scoreDTOs);
            User winner = userService.findById(winnerId)
                    .orElseThrow(() -> new NotFoundException("Vencedor não encontrado"));
            redirectAttributes.addFlashAttribute("winnerName", winner.getNickname());
            return "redirect:/matches/" + matchId + "/winner";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/matches/" + matchId;
        }
    }

    @PostMapping("/{matchId}/cancel")
    public String cancelMatch(
            @PathVariable("matchId") UUID matchId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            matchService.cancelMatch(matchId);
            playerScoreService.cancelMatchScores(matchId);
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

    @GetMapping("/my-matches")
    public String getUserMatches(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        Pageable pageable = PageRequest.of(page, size);
        Page<PlayerMatchDTO> matchesPage = playerScoreService.getUserMatches(user.getId(), pageable);
        int totalPoints = playerScoreService.sumTotalPointsByUserId(user.getId());
        model.addAttribute("matchesPage", matchesPage);
        model.addAttribute("totalPoints", totalPoints);
        return "user-matches";
    }

    @GetMapping("/{matchId}/details")
    @ResponseBody
    public ResponseEntity<MatchDetailsDTO> getMatchDetails(
            @PathVariable("matchId") UUID matchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            MatchDetailsDTO details = playerScoreService.getMatchDetails(matchId);
            details.setCurrentUserId(currentUser.getId());

            return ResponseEntity.ok(details);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao buscar detalhes da partida", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{matchId}/score")
    public String showScoreForm(
            @PathVariable("matchId") UUID matchId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User currentUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Match match = matchService.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada"));
        if (!gameGroupService.isUserInGroup(currentUser.getId(), match.getGroup().getId())) {
            throw new AccessDeniedException("Acesso não autorizado à partida");
        }
        GameGroup group = match.getGroup();
        if (group == null) {
            throw new IllegalStateException("Grupo não encontrado para esta partida");
        }
        int currentRound = playerScoreService.getNextRoundNumber(matchId);
        List<PlayerScore> roundsHistory = playerScoreService.getRoundsHistory(matchId);
        Integer teamAScore = playerScoreService.getTotalScoreByTeam(matchId, "A");
        Integer teamBScore = playerScoreService.getTotalScoreByTeam(matchId, "B");
        boolean hasWinner = match.getStatus() == MatchStatus.FINISHED;
        if (match.getGameMode() == GameMode.TEAMS) {
            List<User> teamAPlayers = gameGroupService.getTeamPlayers(group.getId(), "A");
            List<User> teamBPlayers = gameGroupService.getTeamPlayers(group.getId(), "B");
            model.addAttribute("teamA", teamAPlayers);
            model.addAttribute("teamB", teamBPlayers);
            List<UUID> teamAPlayerIds = teamAPlayers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            List<UUID> teamBPlayerIds = teamBPlayers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            model.addAttribute("teamAPlayerIds", teamAPlayerIds);
            model.addAttribute("teamBPlayerIds", teamBPlayerIds);
        }
        if (match.getGameMode() == GameMode.INDIVIDUAL) {
            Map<UUID, Integer> latestScores = playerScoreService.getRoundsHistory(matchId).stream()
                    .collect(Collectors.groupingBy(ps -> ps.getUser().getId(),
                            Collectors.collectingAndThen(
                                    Collectors.maxBy(Comparator.comparingInt(PlayerScore::getRoundNumber)),
                                    opt -> opt.map(PlayerScore::getTotalScore).orElse(0)
                            )));
            model.addAttribute("latestScores", latestScores);
            model.addAttribute("players", gameGroupService.getPlayersInGroup(group.getId()));
        }
        model.addAttribute("match", match);
        model.addAttribute("group", group);
        model.addAttribute("gameMode", match.getGameMode());
        model.addAttribute("isCreator", group.getCreatedBy().getId().equals(currentUser.getId()));
        model.addAttribute("currentRound", currentRound);
        model.addAttribute("roundsHistory", roundsHistory);
        model.addAttribute("teamAScore", teamAScore != null ? teamAScore : 0);
        model.addAttribute("teamBScore", teamBScore != null ? teamBScore : 0);
        model.addAttribute("hasWinner", hasWinner);
        return "score-form";
    }

    @PostMapping("/{matchId}/save-round")
    public String saveRound(
            @PathVariable("matchId") UUID matchId,
            @RequestParam("roundNumber") Integer roundNumber,
            @RequestParam(value = "teamAScore", defaultValue = "0") Integer teamAScore,
            @RequestParam(value = "teamBScore", defaultValue = "0") Integer teamBScore,
            @RequestParam("teamAPlayerIds") String teamAPlayerIdsStr,
            @RequestParam("teamBPlayerIds") String teamBPlayerIdsStr,
            RedirectAttributes redirectAttributes) {
        try {
            List<UUID> teamAPlayerIds = Arrays.stream(teamAPlayerIdsStr.split(","))
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            List<UUID> teamBPlayerIds = Arrays.stream(teamBPlayerIdsStr.split(","))
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            playerScoreService.saveTeamRound(matchId, roundNumber,
                    teamAScore, teamBScore, teamAPlayerIds, teamBPlayerIds);
            redirectAttributes.addFlashAttribute("success", "Rodada salva com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao salvar rodada", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao salvar rodada: " + e.getMessage());
        }
        return "redirect:/matches/" + matchId + "/score";
    }

    @PostMapping("/{matchId}/save-individual-scores")
    public String saveIndividualScores(
            @PathVariable("matchId") UUID matchId,
            @RequestParam("playerIds[]") List<UUID> playerIds,
            @RequestParam("scores[]") List<Integer> scores,
            @RequestParam("roundNumber") Integer roundNumber,
            RedirectAttributes redirectAttributes) {
        try {
            if (playerIds.isEmpty()) {
                throw new IllegalArgumentException("No players provided.");
            }
            Match match = matchService.findById(matchId)
                    .orElseThrow(() -> new NotFoundException("Partida não encontrada"));
            if (match.getStatus() != MatchStatus.IN_PROGRESS) {
                throw new IllegalStateException("Partida já está " + match.getStatus());
            }
            if (playerIds.size() != scores.size()) {
                throw new IllegalArgumentException("Número de jogadores e pontuações não corresponde");
            }
            List<ScoreDTO> scoreDTOs = IntStream.range(0, playerIds.size())
                    .mapToObj(i -> new ScoreDTO(playerIds.get(i), scores.get(i)))
                    .collect(Collectors.toList());
            playerScoreService.saveIndividualRound(matchId, roundNumber, scoreDTOs);
            redirectAttributes.addFlashAttribute("success", "Pontuações salvas com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao salvar pontuações individuais", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao salvar pontuações: " + e.getMessage());
        }
        return "redirect:/matches/" + matchId + "/score";
    }
}