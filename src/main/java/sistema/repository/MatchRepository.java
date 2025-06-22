package sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistema.model.Match;
import sistema.model.MatchStatus;

import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByGroupIdAndStatus(UUID groupId,  MatchStatus status);
}
