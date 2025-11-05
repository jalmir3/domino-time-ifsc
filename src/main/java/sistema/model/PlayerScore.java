package sistema.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "player_scores")
@Data
@NoArgsConstructor
public class PlayerScore {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, columnDefinition = "UUID")
    private Match match;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "UUID")
    private User user;
    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;
    @Column(name = "current_round", nullable = false)
    private Integer currentRound = 1;
    @Column(name = "team", length = 1)
    private String team;
    @Column(name = "is_winner")
    private Boolean isWinner = false;
    @Column(name = "score")
    private Integer score;
    @Column(name = "round_number")
    private Integer roundNumber;
    @Column(name = "player_name", length = 100)
    private String playerName;
}