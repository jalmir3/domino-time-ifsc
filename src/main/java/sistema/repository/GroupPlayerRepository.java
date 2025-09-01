package sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistema.model.GroupPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupPlayerRepository extends JpaRepository<GroupPlayer, UUID> {
    boolean existsByUserIdAndGroupId(UUID userId, UUID groupId);

    List<GroupPlayer> findByGroupId(UUID groupId);

    long countByGroupId(UUID groupId);

    List<GroupPlayer> findByGroupIdAndTeam(UUID groupId, String team);

    Optional<GroupPlayer> findByGroupIdAndUserId(UUID groupId, UUID userId);
}