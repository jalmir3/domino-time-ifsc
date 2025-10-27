package sistema.service;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sistema.dto.UserRegistrationDTO;
import sistema.model.User;
import sistema.model.UserStatus;
import sistema.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public void registerUserWithActivation(UserRegistrationDTO registrationDto) throws MessagingException {
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Senhas não coincidem");
        }
        if (userRepository.findByEmailAndDeletedFalse(registrationDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        if (userRepository.findByNicknameAndDeletedFalse(registrationDto.getNickname()).isPresent()) {
            throw new IllegalArgumentException("Apelido já cadastrado");
        }
        String activationToken = UUID.randomUUID().toString();
        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setActivationToken(activationToken);
        user.setBirthDate(registrationDto.getBirthDate());
        user.setNickname(registrationDto.getNickname());
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        String activationLink = "http://localhost:8080/activate?token=" + activationToken;
        emailService.sendActivationEmail(user.getEmail(), activationLink);
    }

    public boolean activateUser(String token) {
        return userRepository.findByActivationToken(token)
                .map(user -> {
                    user.setStatus(UserStatus.ACTIVE);
                    user.setActivatedAt(LocalDateTime.now());
                    user.setActivationToken(null);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email não cadastrado"));
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        return token;
    }

    public boolean isPasswordResetTokenValid(String token) {
        return userRepository.findByPasswordResetToken(token)
                .map(user -> user.getPasswordResetExpiry().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .filter(u -> u.getPasswordResetExpiry().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(UUID winnerId) {
        return userRepository.findById(winnerId);
    }

    public boolean isNicknameInUseByOtherUser(String nickname, UUID currentUserId) {
        return userRepository.findByNicknameAndDeletedFalse(nickname)
                .map(user -> !user.getId().equals(currentUserId))
                .orElse(false);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Transactional
    public void deleteUser(UUID userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Senha incorreta");
        }
        user.setStatus(UserStatus.DELETED);
        user.setEmail(user.getId() + "@deleted.com");
        user.setNickname(user.getNickname()+"(deletado)");
        user.setDeletedAt(LocalDateTime.now());
        user.setAvatar(null);
        userRepository.save(user);
        SecurityContextHolder.clearContext();
    }
}
