package sistema.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@SQLDelete(sql = "UPDATE users SET deleted = true, deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted = false")
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    @Column(name = "nickname", unique = true, nullable = false, length = 100)
    private String nickname;
    @Column(name = "birth_date", nullable = false, columnDefinition = "DATE")
    private LocalDate birthDate;
    @Column(name = "password", nullable = false, length = 100)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(20) DEFAULT 'INACTIVE'")
    private UserStatus status = UserStatus.INACTIVE;
    @Column(name = "activation_token", length = 50)
    private String activationToken;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime createdAt;
    @Column(name = "activated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime activatedAt;
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    @Column(name = "password_reset_expiry")
    private LocalDateTime passwordResetExpiry;
    @Column(name = "avatar", columnDefinition = "TEXT")
    private String avatar;
    @Column(name = "deleted")
    private boolean deleted = false;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
