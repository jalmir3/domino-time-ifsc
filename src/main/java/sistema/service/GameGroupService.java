package sistema.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import sistema.model.*;
import sistema.repository.GameGroupRepository;
import sistema.repository.GroupPlayerRepository;
import sistema.repository.MatchRepository;
import sistema.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameGroupService {
    private final GameGroupRepository gameGroupRepository;
    private final GroupPlayerRepository groupPlayerRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    public GameGroup createGroup(User creator, String groupName) {
        GameGroup group = new GameGroup();
        group.setName(groupName);
        group.setCreatedBy(creator);
        group.setAccessCode(generateRandomCode());
        var savedGroup = gameGroupRepository.save(group);
        GroupPlayer creatorMembership = new GroupPlayer();
        creatorMembership.setUser(creator);
        creatorMembership.setGroup(savedGroup);
        groupPlayerRepository.save(creatorMembership);
        return savedGroup;
    }

    private String generateRandomCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public List<Match> getActiveMatches(UUID groupId) {
        return matchRepository.findByGroupIdAndStatus(groupId, MatchStatus.IN_PROGRESS);
    }

    public boolean isUserInGroup(UUID userId, UUID groupId) {
        return groupPlayerRepository.existsByUserIdAndGroupId(userId, groupId);
    }

    public List<User> getPlayersInGroup(UUID groupId) {
        return groupPlayerRepository.findByGroupId(groupId).stream()
                .map(GroupPlayer::getUser)
                .collect(Collectors.toList());
    }

    public void addPlayerToGroup(UUID userId, String accessCode) {
        GameGroup group = gameGroupRepository.findByAccessCode(accessCode)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        long playerCount = groupPlayerRepository.countByGroupId(group.getId());
        if (playerCount >= 4) {
            throw new IllegalStateException("A partida já está cheia (4 jogadores)");
        }
        if (groupPlayerRepository.existsByUserIdAndGroupId(userId, group.getId())) {
            throw new IllegalArgumentException("Você já está na partida");
        }
        GroupPlayer groupPlayer = new GroupPlayer();
        groupPlayer.setGroup(group);
        groupPlayer.setUser(user);
        groupPlayerRepository.save(groupPlayer);
    }

    public List<User> getTeamPlayers(UUID groupId, String team) {
        return groupPlayerRepository.findByGroupIdAndTeam(groupId, team).stream()
                .map(GroupPlayer::getUser)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveTeams(UUID groupId, List<UUID> teamAPlayerIds, List<UUID> teamBPlayerIds) {
        for (UUID playerId : teamAPlayerIds) {
            GroupPlayer groupPlayer = groupPlayerRepository.findByGroupIdAndUserId(groupId, playerId)
                    .orElseThrow(() -> new NotFoundException("Jogador não encontrado no grupo"));
            groupPlayer.setTeam("A");
            groupPlayerRepository.save(groupPlayer);
        }
        for (UUID playerId : teamBPlayerIds) {
            GroupPlayer groupPlayer = groupPlayerRepository.findByGroupIdAndUserId(groupId, playerId)
                    .orElseThrow(() -> new NotFoundException("Jogador não encontrado no grupo"));
            groupPlayer.setTeam("B");
            groupPlayerRepository.save(groupPlayer);
        }
    }
}