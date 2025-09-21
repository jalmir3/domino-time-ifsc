package sistema.service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import sistema.dto.PlayerMatchDTO;
import sistema.dto.ScoreDTO;
import sistema.model.*;
import sistema.repository.MatchRepository;
import sistema.repository.PlayerScoreRepository;
import sistema.repository.RoundHistoryRepository;
import sistema.repository.UserRepository;
import java.util.*;
@Service
@Slf4j
@RequiredArgsConstructor
public class PlayerScoreService {
    private final PlayerScoreRepository playerScoreRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final RoundHistoryRepository roundHistoryRepository;
    private static final int WINNING_THRESHOLD = 100;
    public UUID saveScoresAndGetWinner(UUID matchId, List<ScoreDTO> scores) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada"));
        ScoreDTO winnerDto = scores.stream()
                .max(Comparator.comparingInt(ScoreDTO::score))
                .orElseThrow(() -> new RuntimeException("Não foi possível determinar o vencedor"));
        if (winnerDto.score() < WINNING_THRESHOLD) {
            throw new IllegalStateException("Nenhum jogador atingiu " + WINNING_THRESHOLD + " pontos");
        }
        scores.forEach(dto -> {
            User player = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new NotFoundException("Jogador não encontrado: " + dto.userId()));
            PlayerScore score = new PlayerScore();
            score.setMatch(match);
            score.setUser(player);
            score.setScore(dto.score());
            score.setTotalScore(dto.score());
            score.setIsWinner(dto.equals(winnerDto));
            playerScoreRepository.save(score);
        });
        match.setStatus(MatchStatus.FINISHED);
        matchRepository.save(match);
        return winnerDto.userId();
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
        dto.setPlayerScore(score.getTotalScore());
        dto.setWinner(score.getIsWinner());
        dto.setMatchDate(score.getMatch().getMatchDate());
        return dto;
    }
    @Transactional
    public void saveTeamRound(UUID matchId, Integer roundNumber, Integer teamAScore,
                              Integer teamBScore, List<UUID> teamAPlayerIds, List<UUID> teamBPlayerIds) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada"));
        int safeTeamAScore = (teamAScore != null) ? teamAScore : 0;
        int safeTeamBScore = (teamBScore != null) ? teamBScore : 0;
        for (UUID playerId : teamAPlayerIds) {
            User player = userRepository.findById(playerId)
                    .orElseThrow(() -> new NotFoundException("Jogador não encontrado"));
            Optional<PlayerScore> existingScoreOpt = playerScoreRepository.findByMatchIdAndUserId(matchId, playerId);
            PlayerScore playerScore = existingScoreOpt.orElseGet(() -> {
                PlayerScore newScore = new PlayerScore();
                newScore.setMatch(match);
                newScore.setUser(player);
                newScore.setTeam("A");
                return newScore;
            });
            playerScore.setTotalScore(playerScore.getTotalScore() + safeTeamAScore);
            playerScore.setScore(safeTeamAScore);
            playerScore.setCurrentRound(roundNumber);
            playerScore.setRoundNumber(roundNumber);
            playerScoreRepository.save(playerScore);
        }
        for (UUID playerId : teamBPlayerIds) {
            User player = userRepository.findById(playerId)
                    .orElseThrow(() -> new NotFoundException("Jogador não encontrado"));
            Optional<PlayerScore> existingScoreOpt = playerScoreRepository.findByMatchIdAndUserId(matchId, playerId);
            PlayerScore playerScore = existingScoreOpt.orElseGet(() -> {
                PlayerScore newScore = new PlayerScore();
                newScore.setMatch(match);
                newScore.setUser(player);
                newScore.setTeam("B");
                return newScore;
            });
            playerScore.setTotalScore(playerScore.getTotalScore() + safeTeamBScore);
            playerScore.setScore(safeTeamBScore);
            playerScore.setCurrentRound(roundNumber);
            playerScore.setRoundNumber(roundNumber);
            playerScoreRepository.save(playerScore);
        }
        RoundHistory roundHistory = new RoundHistory();
        roundHistory.setMatch(match);
        roundHistory.setRoundNumber(roundNumber);
        roundHistory.setTeamAScore(safeTeamAScore);
        roundHistory.setTeamBScore(safeTeamBScore);
        roundHistoryRepository.save(roundHistory);
        checkAndFinishMatch(matchId, match.getGameMode());
    }
    @Transactional
    public void saveIndividualRound(UUID matchId, Integer roundNumber, List<ScoreDTO> scoreDTOs) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada"));
        for (ScoreDTO dto : scoreDTOs) {
            User player = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new NotFoundException("Jogador não encontrado: " + dto.userId()));
            Optional<PlayerScore> existingScoreOpt = playerScoreRepository.findByMatchIdAndUserId(matchId, dto.userId());
            PlayerScore playerScore = existingScoreOpt.orElseGet(() -> {
                PlayerScore newScore = new PlayerScore();
                newScore.setMatch(match);
                newScore.setUser(player);
                return newScore;
            });
            playerScore.setTotalScore(playerScore.getTotalScore() + dto.score());
            playerScore.setScore(dto.score());
            playerScore.setCurrentRound(roundNumber);
            playerScore.setRoundNumber(roundNumber);
            playerScoreRepository.save(playerScore);
        }
        checkAndFinishMatch(matchId, match.getGameMode());
    }
    public Integer getTotalScoreByTeam(UUID matchId, String team) {
        return playerScoreRepository.sumScoresByMatchAndTeam(matchId, team);
    }
    private void checkAndFinishMatch(UUID matchId, GameMode gameMode) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada"));
        if (gameMode == GameMode.TEAMS) {
            Integer totalScoreA = getTotalScoreByTeam(matchId, "A");
            Integer totalScoreB = getTotalScoreByTeam(matchId, "B");
            totalScoreA = totalScoreA != null ? totalScoreA : 0;
            totalScoreB = totalScoreB != null ? totalScoreB : 0;
            if (totalScoreA >= WINNING_THRESHOLD || totalScoreB >= WINNING_THRESHOLD) {
                String winner = totalScoreA >= WINNING_THRESHOLD ? "Time A" : "Time B";
                match.setWinner(winner);
                match.setStatus(MatchStatus.FINISHED);
                match.setFinalScoreA(totalScoreA);
                match.setFinalScoreB(totalScoreB);
                matchRepository.save(match);
                markWinners(matchId, winner.equals("Time A") ? "A" : "B");
            }
        } else {
            List<PlayerScore> scores = playerScoreRepository.findByMatchId(matchId);
            Optional<PlayerScore> winnerOpt = scores.stream()
                    .filter(ps -> ps.getTotalScore() >= WINNING_THRESHOLD)
                    .max(Comparator.comparingInt(PlayerScore::getTotalScore));
            if (winnerOpt.isPresent()) {
                PlayerScore winner = winnerOpt.get();
                match.setWinner(winner.getUser().getNickname());
                match.setStatus(MatchStatus.FINISHED);
                matchRepository.save(match);
                scores.forEach(ps -> {
                    ps.setIsWinner(ps.getUser().getId().equals(winner.getUser().getId()));
                    playerScoreRepository.save(ps);
                });
            }
        }
    }
    private void markWinners(UUID matchId, String winningTeam) {
        List<PlayerScore> winningScores = playerScoreRepository.findByMatchIdAndTeam(matchId, winningTeam);
        winningScores.forEach(score -> score.setIsWinner(true));
        playerScoreRepository.saveAll(winningScores);
    }
    public Integer getNextRoundNumber(UUID matchId) {
        Integer maxRound = playerScoreRepository.findMaxRoundNumberByMatchId(matchId);
        return maxRound != null ? maxRound + 1 : 1;
    }
    @Transactional
    public void cancelMatchScores(UUID matchId) {
        playerScoreRepository.deleteByMatchId(matchId);
        roundHistoryRepository.deleteByMatchId(matchId);
    }
    public List<PlayerScore> getRoundsHistory(UUID matchId) {
        return playerScoreRepository.findByMatchIdOrderByRoundNumberAsc(matchId);
    }
}