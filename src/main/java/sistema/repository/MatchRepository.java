package sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistema.model.Match;

import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
}
