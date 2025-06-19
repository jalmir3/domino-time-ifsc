package sistema.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import sistema.dto.ScoreDTO;
import sistema.model.*;
import sistema.repository.MatchRepository;
import sistema.repository.PlayerScoreRepository;
import sistema.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final PlayerScoreService playerScoreService;
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
    public void saveScores(UUID matchId, List<ScoreDTO> scores) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        int maxScore = scores.stream()
                .mapToInt(ScoreDTO::score)
                .max()
                .orElse(0);

        scores.forEach(dto -> {
            User player = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new RuntimeException("Player not found: " + dto.userId()));

            PlayerScore score = new PlayerScore();
            score.setMatch(match);
            score.setUser(player);
            score.setScore(dto.score());
            score.setIsWinner(dto.score() == maxScore);

            playerScoreRepository.save(score);
        });

        match.setStatus(MatchStatus.FINISHED);
        matchRepository.save(match);
    }

    @Transactional
    public Match cancelMatch(UUID matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada"));

        GameGroup group = match.getGroup();
        if (group == null) {
            throw new IllegalStateException("Partida não está associada a um grupo válido");
        }

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new IllegalStateException("A partida já está " + match.getStatus());
        }

        match.setStatus(MatchStatus.CANCELED);

        Match updatedMatch = matchRepository.save(match);

        if (updatedMatch.getStatus() != MatchStatus.CANCELED) {
            throw new IllegalStateException("Falha ao atualizar status da partida");
        }

        return updatedMatch;
    }
}
