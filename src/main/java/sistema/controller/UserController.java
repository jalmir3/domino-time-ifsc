package sistema.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import sistema.repository.PlayerScoreRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Controller
public class UserController {
    @Autowired
    private PlayerScoreRepository playerScoreRepository;
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/home")
    public String home() {
        return "home";
    }
    @ModelAttribute("topPlayers")
    public List<Map<String, Object>> getTopPlayers() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Object[]> topPlayersPage = playerScoreRepository.findTop10UsersByTotalScore(pageRequest);
        List<Map<String, Object>> topPlayers = new ArrayList<>();
        int position = 1;
        for (Object[] result : topPlayersPage.getContent()) {
            Map<String, Object> playerInfo = new HashMap<>();
            playerInfo.put("position", position++);
            playerInfo.put("nickname", result[0]);
            playerInfo.put("totalScore", result[1]);
            topPlayers.add(playerInfo);
        }
        return topPlayers;
    }
}