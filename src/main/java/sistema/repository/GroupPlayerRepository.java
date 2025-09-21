package sistema.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Transactional
    @Modifying
    @Query("DELETE FROM GroupPlayer gp WHERE gp.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}