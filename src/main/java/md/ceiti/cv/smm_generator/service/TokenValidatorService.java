package md.ceiti.cv.smm_generator.service;

import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenValidatorService {

    private final UserRepository userRepository;

    public TokenValidatorService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isFacebookTokenExpired(User user) {
        return user.getFacebookTokenExpiry() == null ||
                user.getFacebookTokenExpiry().isBefore(LocalDateTime.now());
    }

    public boolean isFacebookTokenExpired(Principal principal) {
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        return optionalUser.map(this::isFacebookTokenExpired).orElse(true);
    }

    public User getUserByPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }
}