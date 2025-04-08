package sistema.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sistema.dto.UserRegistrationDto;
import sistema.model.User;
import sistema.model.UserStatus;
import sistema.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User registerUser(UserRegistrationDto registrationDto) {
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Senhas não coincidem");
        }

        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setBirthDate(registrationDto.getBirthDate());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setStatus(UserStatus.INACTIVE);
        user.setActivationPin(generatePin());

        User savedUser = userRepository.save(user);

        emailService.sendActivationEmail(savedUser.getEmail(), savedUser.getActivationPin());

        return savedUser;
    }

    public boolean activateUser(String email, String pin) {
        return userRepository.findByEmailAndActivationPin(email, pin)
                .map(user -> {
                    user.setStatus(UserStatus.ACTIVE);
                    user.setActivatedAt(LocalDateTime.now());
                    user.setActivationPin(null);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    private String generatePin() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
