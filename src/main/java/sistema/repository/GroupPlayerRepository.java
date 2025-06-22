package sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sistema.model.GroupPlayer;
import sistema.model.UserStatus;

import java.util.List;
import java.util.UUID;

public interface GroupPlayerRepository extends JpaRepository<GroupPlayer, UUID> {
    boolean existsByUserIdAndGroupId(UUID userId, UUID groupId);

    List<GroupPlayer> findByGroupId(UUID groupId);

    @Query("SELECT COUNT(gp) FROM GroupPlayer gp WHERE gp.group.id = :groupId AND gp.user.status = :status")
    long countByGroupIdAndUserStatus(@Param("groupId") UUID groupId, @Param("status") UserStatus status);
}