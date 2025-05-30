package sistema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sistema.model.*;
import sistema.repository.*;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameGroupService {

    private final GameGroupRepository groupRepository;
    private final GroupPlayerRepository groupPlayerRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final PlayerScoreRepository playerScoreRepository;

    public GameGroup createGroup(User creator, String groupName, GroupType groupType) {
        GameGroup group = new GameGroup();
        group.setName(groupName);
        group.setCreatedBy(creator);
        group.setAccessCode(generateRandomCode());
        group.setPrivacy(groupType);
        return groupRepository.save(group);
    }

    private String generateRandomCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void addPlayerToGroup(User user, String accessCode) {
        GameGroup group = groupRepository.findByAccessCode(accessCode)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        GroupPlayer player = new GroupPlayer();
        player.setUser(user);
        player.setGroup(group);
        groupPlayerRepository.save(player);
    }

    public Match startMatch(UUID groupId, User createdBy) {
        GameGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        Match match = new Match();
        match.setGroup(group);
        match.setStatus(MatchStatus.IN_PROGRESS);
        return matchRepository.save(match);
    }

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
        return playerScoreRepository.save(playerScore);
    }
}
