package sistema.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import sistema.model.*;
import sistema.repository.GameGroupRepository;
import sistema.repository.MatchRepository;
import sistema.repository.PlayerScoreRepository;
import sistema.repository.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final GameGroupRepository groupRepository;
    private final PlayerScoreRepository playerScoreRepository;
    private final UserRepository userRepository;

    public Match startNewMatch(GameGroup group, User creator) {
        if (!group.getCreatedBy().getId().equals(creator.getId())) {
            throw new AccessDeniedException("Apenas o criador pode iniciar partidas");
        }

        Match match = new Match();
        match.setGroup(group);
        match.setStatus(MatchStatus.IN_PROGRESS);
        return matchRepository.save(match);
    }

    public Optional<Match> findById(UUID matchId) {
        return matchRepository.findById(matchId);
    }

    @Transactional
    public void saveMatchScores(UUID matchId, User creator, Map<String, String> scores, Set<String> winnerIds) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));

        if (!match.getGroup().getCreatedBy().getId().equals(creator.getId())) {
            throw new AccessDeniedException("Apenas o criador pode salvar pontuações");
        }

        playerScoreRepository.deleteByMatch(match);

        scores.forEach((playerIdStr, scoreStr) -> {
            try {
                UUID playerId = UUID.fromString(playerIdStr);
                int score = Integer.parseInt(scoreStr);
                boolean isWinner = winnerIds.contains(playerIdStr);

                User player = userRepository.findById(playerId)
                        .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));

                PlayerScore playerScore = new PlayerScore();
                playerScore.setMatch(match);
                playerScore.setUser(player);
                playerScore.setScore(score);
                playerScore.setIsWinner(isWinner);

                playerScoreRepository.save(playerScore);
            } catch (Exception e) {
                throw new IllegalArgumentException("Dados de pontuação inválidos");
            }
        });

        match.setStatus(MatchStatus.FINISHED);
        matchRepository.save(match);
    }
}
