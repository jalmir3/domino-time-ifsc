package sistema.service;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sistema.dto.UserRegistrationDto;
import sistema.model.User;
import sistema.model.UserStatus;
import sistema.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public String registerUserWithActivation(UserRegistrationDto registrationDto) throws MessagingException {
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Senhas não coincidem");
        }

        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if (userRepository.findByNickname(registrationDto.getNickname()).isPresent()) {
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

        return activationToken;
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
}
