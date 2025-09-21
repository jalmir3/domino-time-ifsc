package sistema.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
public class Match {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, columnDefinition = "UUID")
    private GameGroup group;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(20) DEFAULT 'IN_PROGRESS'")
    private MatchStatus status = MatchStatus.IN_PROGRESS;
    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate = LocalDate.now();
    @Enumerated(EnumType.STRING)
    @Column(name = "game_mode", columnDefinition = "VARCHAR(20) DEFAULT 'INDIVIDUAL'")
    private GameMode gameMode;
    @Column(name = "winner", nullable = true)
    private String winner;
    @Column(name = "final_score_teamA", nullable = true)
    private Integer finalScoreA;
    @Column(name = "final_score_teamB", nullable = true)
    private Integer finalScoreB;
}