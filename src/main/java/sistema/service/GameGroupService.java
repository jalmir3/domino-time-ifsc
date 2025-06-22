package sistema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import sistema.model.*;
import sistema.repository.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameGroupService {

    private final GameGroupRepository groupRepository;
    private final GroupPlayerRepository groupPlayerRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    public GameGroup createGroup(User creator, String groupName, GroupType groupType) {
        GameGroup group = new GameGroup();
        group.setName(groupName);
        group.setCreatedBy(creator);
        group.setAccessCode(generateRandomCode());
        group.setPrivacy(groupType);

        var savedGroup = groupRepository.save(group);

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
        GameGroup group = groupRepository.findByAccessCode(accessCode)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada"));

        boolean hasActiveMatch = !matchRepository.findByGroupIdAndStatus(group.getId(), MatchStatus.IN_PROGRESS).isEmpty();

        if (!hasActiveMatch) {
            throw new IllegalStateException("Partida Finalizada/Cancelada");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (groupPlayerRepository.existsByUserIdAndGroupId(userId,group.getId())) {
            throw new IllegalArgumentException("Você já está na partida");
        }

        GroupPlayer groupPlayer = new GroupPlayer();
        groupPlayer.setGroup(group);
        groupPlayer.setUser(user);
        groupPlayerRepository.save(groupPlayer);
    }
}
