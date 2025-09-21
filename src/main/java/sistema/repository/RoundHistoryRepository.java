package sistema.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sistema.model.RoundHistory;
import java.util.UUID;
public interface RoundHistoryRepository extends JpaRepository<RoundHistory, UUID> {
    @Modifying
    @Query("DELETE FROM RoundHistory rh WHERE rh.match.id = :matchId")
    void deleteByMatchId(@Param("matchId") UUID matchId);
}