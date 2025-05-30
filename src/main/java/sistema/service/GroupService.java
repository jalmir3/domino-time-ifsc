package sistema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sistema.model.GameGroup;
import sistema.model.GroupPlayer;
import sistema.model.User;
import sistema.repository.GameGroupRepository;
import sistema.repository.GroupPlayerRepository;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GameGroupRepository groupRepository;
    private final GroupPlayerRepository groupPlayerRepository;

    public GameGroup findByAccessCode(String accessCode) {
        return groupRepository.findByAccessCode(accessCode)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));
    }

    public void addPlayerToGroup(User user, String accessCode) {
        GameGroup group = findByAccessCode(accessCode);
        if (!groupPlayerRepository.existsByUserAndGroup(user, group)) {
            GroupPlayer player = new GroupPlayer();
            player.setUser(user);
            player.setGroup(group);
            groupPlayerRepository.save(player);
        }
    }
}
