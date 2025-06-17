package md.ceiti.cv.smm_generator.service;

import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OAuthService {
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuthService(UserRepository userRepository, OAuth2AuthorizedClientService authorizedClientService) {
        this.userRepository = userRepository;
        this.authorizedClientService = authorizedClientService;
    }

    public boolean saveFacebookToken(OAuth2AuthenticationToken authentication) {
        OAuth2User userDetails = authentication.getPrincipal();
        String email = userDetails.getAttribute("email");
        String facebookId = userDetails.getAttribute("id");

        System.out.println("Facebook login detected");
        System.out.println("Email from Facebook: " + email);
        System.out.println("Facebook ID: " + facebookId);

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (client != null && email != null) {
            String accessToken = client.getAccessToken().getTokenValue();
            System.out.println("Access Token: " + accessToken);

            Optional<User> optionalUser = userRepository.findByEmail(email);

            return optionalUser.map(user -> {
                System.out.println("User found in DB: " + user.getEmail());
                user.setFacebookAccessToken(accessToken);
                user.setFacebookUserId(facebookId);
                userRepository.save(user);
                System.out.println("Token saved in database");
                return true;
            }).orElseGet(() -> {
                System.out.println("User with email " + email + " not found in DB");
                return false;
            });
        } else {
            if (client == null) {
                System.out.println("OAuth2AuthorizedClient is null");
            } else {
                System.out.println("Email is null - scope=email missing?");
            }
        }

        return false;
    }
}