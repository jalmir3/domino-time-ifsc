package sistema.dto;

import lombok.Data;
import sistema.model.GameMode;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class MatchDetailsDTO {
    private UUID matchId;
    private String groupName;
    private LocalDate matchDate;
    private GameMode gameMode;
    private String winner;
    private List<PlayerDetailDTO> players;
    private UUID currentUserId;

    @Data
    public static class PlayerDetailDTO {
        private UUID userId;
        private String nickname;
        private Integer score;
        private String team;
        private boolean isWinner;
    }
}
