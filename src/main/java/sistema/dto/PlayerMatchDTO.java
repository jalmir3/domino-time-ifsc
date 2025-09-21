package sistema.dto;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;
@Data
public class PlayerMatchDTO {
    private UUID matchId;
    private String groupName;
    private Integer playerScore;
    private boolean winner;
    private LocalDate matchDate;
}
