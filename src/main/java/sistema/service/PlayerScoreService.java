package sistema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import sistema.dto.PlayerMatchDTO;
import sistema.dto.ScoreDTO;
import sistema.model.Match;
import sistema.model.MatchStatus;
import sistema.model.PlayerScore;
import sistema.model.User;
import sistema.repository.MatchRepository;
import sistema.repository.PlayerScoreRepository;
import sistema.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerScoreService {

    private final PlayerScoreRepository playerScoreRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    public UUID saveScoresAndGetWinner(UUID matchId, List<ScoreDTO> scores) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada"));

        ScoreDTO winnerDto = scores.stream()
                .max(Comparator.comparingInt(ScoreDTO::score))
                .orElseThrow(() -> new RuntimeException("Não foi possível determinar o vencedor"));

        scores.forEach(dto -> {
            User player = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new NotFoundException("Jogador não encontrado: " + dto.userId()));

            PlayerScore score = new PlayerScore();
            score.setMatch(match);
            score.setUser(player);
            score.setScore(dto.score());
            score.setIsWinner(dto.equals(winnerDto));

            playerScoreRepository.save(score);
        });

        match.setStatus(MatchStatus.FINISHED);
        matchRepository.save(match);

        return winnerDto.userId();
    }

    public Optional<User> findWinnerByMatchId(UUID matchId) {
        return playerScoreRepository.findWinnerByMatchId(matchId);
    }

    public int sumTotalPointsByUserId(UUID id) {
        return playerScoreRepository.sumScoresByUserId(id);
    }

    public Page<PlayerMatchDTO> getUserMatches(UUID userId, Pageable pageable) {

        Page<PlayerScore> scoresPage = playerScoreRepository.findByUserId(userId, pageable);

        return scoresPage.map(this::convertToDTO);
    }

    private PlayerMatchDTO convertToDTO(PlayerScore score) {
        PlayerMatchDTO dto = new PlayerMatchDTO();
        dto.setMatchId(score.getMatch().getId());
        dto.setGroupName(score.getMatch().getGroup().getName());
        dto.setPlayerScore(score.getScore());
        dto.setWinner(score.getIsWinner());
        dto.setMatchDate(score.getMatch().getMatchDate());
        return dto;
    }
}
