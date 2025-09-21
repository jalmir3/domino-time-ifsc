package sistema.repository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sistema.model.PlayerScore;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface PlayerScoreRepository extends JpaRepository<PlayerScore, UUID> {
    @Query("SELECT ps FROM PlayerScore ps WHERE ps.user.id = :userId")
    Page<PlayerScore> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    @Query("SELECT COALESCE(SUM(ps.totalScore), 0) FROM PlayerScore ps WHERE ps.user.id = :userId")
    int sumScoresByUserId(@Param("userId") UUID userId);
    @Query("SELECT ps FROM PlayerScore ps WHERE ps.match.id = :matchId")
    List<PlayerScore> findByMatchId(@Param("matchId") UUID matchId);
    @Query("SELECT ps FROM PlayerScore ps WHERE ps.match.id = :matchId AND ps.team = :team")
    List<PlayerScore> findByMatchIdAndTeam(@Param("matchId") UUID matchId, @Param("team") String team);
    @Query("SELECT MAX(ps.roundNumber) FROM PlayerScore ps WHERE ps.match.id = :matchId")
    Integer findMaxRoundNumberByMatchId(@Param("matchId") UUID matchId);
    @Query("SELECT ps FROM PlayerScore ps WHERE ps.match.id = :matchId ORDER BY ps.roundNumber")
    List<PlayerScore> findByMatchIdOrderByRoundNumberAsc(@Param("matchId") UUID matchId);
    @Modifying
    @Query("DELETE FROM PlayerScore ps WHERE ps.match.id = :matchId")
    void deleteByMatchId(@Param("matchId") UUID matchId);
    @Query("SELECT COALESCE(MAX(ps.totalScore), 0) FROM PlayerScore ps WHERE ps.match.id = :matchId AND ps.team = :team")
    Integer sumScoresByMatchAndTeam(@Param("matchId") UUID matchId, @Param("team") String team);
    @Query("SELECT ps FROM PlayerScore ps WHERE ps.match.id = :matchId AND ps.user.id = :userId")
    Optional<PlayerScore> findByMatchIdAndUserId(@Param("matchId") UUID matchId, @Param("userId") UUID userId);
    @Transactional
    @Modifying
    @Query("DELETE FROM PlayerScore ps WHERE ps.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
    @Query("SELECT ps.user.nickname as nickname, SUM(ps.totalScore) as totalScore " +
           "FROM PlayerScore ps " +
           "GROUP BY ps.user.id, ps.user.nickname " +
           "ORDER BY totalScore DESC")
    Page<Object[]> findTop10UsersByTotalScore(Pageable pageable);
}