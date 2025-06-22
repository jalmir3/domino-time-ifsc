package sistema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sistema.model.PlayerScore;
import sistema.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerScoreRepository extends JpaRepository<PlayerScore, UUID> {
    @Query("SELECT ps.user, SUM(ps.score) as totalScore FROM PlayerScore ps GROUP BY ps.user ORDER BY totalScore DESC limit 10")
    List<Object[]> findTopPlayers(Pageable pageable);

    @Query("SELECT ps.user FROM PlayerScore ps WHERE ps.match.id = :matchId AND ps.isWinner = true")
    Optional<User> findWinnerByMatchId(@Param("matchId") UUID matchId);

    @Query("SELECT ps FROM PlayerScore ps " +
            "JOIN FETCH ps.match m " +
            "JOIN FETCH m.group " +
            "WHERE ps.user.id = :userId ")
    Page<PlayerScore> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(ps.score), 0) FROM PlayerScore ps WHERE ps.user.id = :userId")
    int sumScoresByUserId(@Param("userId") UUID userId);
}

