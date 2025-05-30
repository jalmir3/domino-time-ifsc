package sistema.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "game_groups")
@Data
@NoArgsConstructor
public class GameGroup {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "access_code", unique = true, nullable = false, length = 8)
    private String accessCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, columnDefinition = "UUID")
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy", columnDefinition = "VARCHAR(20) DEFAULT 'PUBLIC'")
    private GroupType privacy;
}
