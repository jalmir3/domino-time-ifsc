package sistema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sistema.model.GameGroup;
import sistema.model.Match;
import sistema.model.MatchStatus;
import sistema.model.User;
import sistema.repository.GameGroupRepository;
import sistema.repository.MatchRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final GameGroupRepository groupRepository;

    public Match startMatch(UUID groupId, User createdBy) {
        GameGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));

        Match match = new Match();
        match.setGroup(group);
        match.setStatus(MatchStatus.IN_PROGRESS);
        return matchRepository.save(match);
    }
}
