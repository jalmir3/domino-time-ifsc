package sistema.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "round_history")
@Data
@NoArgsConstructor
public class RoundHistory {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;
    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;
    @Column(name = "team_a_score", nullable = false)
    private Integer teamAScore;
    @Column(name = "team_b_score", nullable = false)
    private Integer teamBScore;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}