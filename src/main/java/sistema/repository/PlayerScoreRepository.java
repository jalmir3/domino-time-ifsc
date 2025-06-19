package sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sistema.model.Match;
import sistema.model.PlayerScore;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

public interface PlayerScoreRepository extends JpaRepository<PlayerScore, UUID> {
    @Query("SELECT ps.user, SUM(ps.score) as totalScore FROM PlayerScore ps GROUP BY ps.user ORDER BY totalScore DESC limit 10")
    List<Object[]> findTopPlayers(Pageable pageable);
    void deleteByMatch(Match match);
    long countByMatchId(UUID matchId);
}

