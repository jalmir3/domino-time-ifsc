package sistema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sistema.model.Match;
import sistema.model.PlayerScore;
import sistema.model.User;
import sistema.repository.MatchRepository;
import sistema.repository.PlayerScoreRepository;
import sistema.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerScoreService {
    private final PlayerScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    public PlayerScore registerScore(UUID matchId, UUID userId, int score, boolean isWinner) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));

        PlayerScore playerScore = new PlayerScore();
        playerScore.setMatch(match);
        playerScore.setUser(user);
        playerScore.setScore(score);
        playerScore.setIsWinner(isWinner);
        return scoreRepository.save(playerScore);
    }
}
