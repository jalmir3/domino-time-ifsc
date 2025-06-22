package sistema.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import sistema.model.GameGroup;
import sistema.model.Match;
import sistema.model.MatchStatus;
import sistema.model.User;
import sistema.repository.MatchRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    public Optional<Match> findById(UUID matchId) {
        return matchRepository.findById(matchId);
    }

    public Match startNewMatch(GameGroup group, User creator) {
        if (!group.getCreatedBy().getId().equals(creator.getId())) {
            throw new AccessDeniedException("Apenas o criador pode iniciar partidas");
        }

        Match match = new Match();
        match.setGroup(group);
        match.setStatus(MatchStatus.IN_PROGRESS);
        return matchRepository.save(match);
    }

    @Transactional
    public Match cancelMatch(UUID matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada"));

        GameGroup group = match.getGroup();
        if (group == null) {
            throw new IllegalStateException("Partida não está associada a um grupo válido");
        }

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new IllegalStateException("A partida já está " + match.getStatus());
        }

        match.setStatus(MatchStatus.CANCELED);
        Match updatedMatch = matchRepository.save(match);

        if (updatedMatch.getStatus() != MatchStatus.CANCELED) {
            throw new IllegalStateException("Falha ao atualizar status da partida");
        }

        return updatedMatch;
    }
}