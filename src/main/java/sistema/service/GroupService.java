package sistema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sistema.model.GameGroup;
import sistema.repository.GameGroupRepository;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GameGroupRepository groupRepository;

    public GameGroup findByAccessCode(String accessCode) {
        return groupRepository.findByAccessCode(accessCode)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado"));
    }
}