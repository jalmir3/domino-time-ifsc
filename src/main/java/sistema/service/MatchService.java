package sistema.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import sistema.model.*;
import sistema.repository.MatchRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final GameGroupService gameGroupService;

    public Optional<Match> findById(UUID matchId) {
        return matchRepository.findById(matchId);
    }

    @Transactional
    public void cancelMatch(UUID matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada"));
        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new IllegalStateException("Não é possível cancelar uma partida finalizada");
        }
        match.setStatus(MatchStatus.CANCELED);
        matchRepository.save(match);
    }

    @Transactional
    public Match startConfiguredMatch(GameGroup group, User creator, GameMode gameMode,
                                      List<UUID> teamA, List<UUID> teamB) {
        if (!group.getCreatedBy().getId().equals(creator.getId())) {
            throw new AccessDeniedException("Apenas o criador do grupo pode iniciar partidas");
        }
        List<User> players = gameGroupService.getPlayersInGroup(group.getId());
        if (players.isEmpty()) {
            throw new IllegalStateException("Não há jogadores no grupo");
        }
        if (gameMode == GameMode.TEAMS) {
            validateTeams(teamA, teamB, players);
        } else if (gameMode == GameMode.INDIVIDUAL) {
            if (players.size() < 2) {
                throw new IllegalStateException("Modo individual requer pelo menos 2 jogadores");
            }
        }
        Match match = new Match();
        match.setGroup(group);
        match.setGameMode(gameMode);
        match.setStatus(MatchStatus.IN_PROGRESS);
        match = matchRepository.save(match);
        if (gameMode == GameMode.TEAMS) {
            gameGroupService.saveTeams(group.getId(), teamA, teamB);
        }
        return match;
    }

    private void validateTeams(List<UUID> teamA, List<UUID> teamB, List<User> allPlayers) {
        if (teamA == null || teamB == null || teamA.size() != 2 || teamB.size() != 2) {
            throw new IllegalArgumentException("Selecione exatamente 2 jogadores para cada time");
        }
        Set<UUID> selectedPlayers = new HashSet<>();
        selectedPlayers.addAll(teamA);
        selectedPlayers.addAll(teamB);
        Set<UUID> actualPlayerIds = allPlayers.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        if (!selectedPlayers.equals(actualPlayerIds)) {
            throw new IllegalArgumentException("Todos os jogadores do grupo devem ser selecionados");
        }
    }
}