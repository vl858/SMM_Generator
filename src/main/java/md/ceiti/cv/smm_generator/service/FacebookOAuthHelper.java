package md.ceiti.cv.smm_generator.service;

import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class FacebookOAuthHelper {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserRepository userRepository;

    // Replace with your real app credentials
    private static final String APP_ID = "YOUR_APP_ID";
    private static final String APP_SECRET = "YOUR_APP_SECRET";

    public FacebookOAuthHelper(OAuth2AuthorizedClientService authorizedClientService,
                               UserRepository userRepository) {
        this.authorizedClientService = authorizedClientService;
        this.userRepository = userRepository;
    }

    public void handleFacebookLogin(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());

        if (client == null || client.getAccessToken() == null) {
            System.out.println("Failed to retrieve access token from Facebook.");
            return;
        }

        String shortLivedToken = client.getAccessToken().getTokenValue();
        System.out.println("Short-lived token: " + shortLivedToken);

        String longLivedToken = exchangeForLongLivedToken(shortLivedToken);
        if (longLivedToken != null) {
            saveTokenToUser(authentication.getName(), longLivedToken);
        }
    }

    private String exchangeForLongLivedToken(String shortToken) {
        String url = "https://graph.facebook.com/v19.0/oauth/access_token" +
                "?grant_type=fb_exchange_token" +
                "&client_id=" + APP_ID +
                "&client_secret=" + APP_SECRET +
                "&fb_exchange_token=" + shortToken;

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("access_token")) {
                System.out.println("Successfully obtained long-lived token.");
                return (String) body.get("access_token");
            } else {
                System.out.println("Invalid response from Facebook: " + body);
            }
        } catch (Exception e) {
            System.out.println("Error exchanging token: " + e.getMessage());
        }
        return null;
    }

    private void saveTokenToUser(String username, String facebookToken) {
        Optional<User> userOptional = userRepository.findByEmail(username); // or findByUsername()
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setFacebookAccessToken(facebookToken);
            user.setFacebookTokenExpiry(LocalDateTime.now().plusDays(60));
            userRepository.save(user);
            System.out.println("Long-lived token saved successfully.");
        } else {
            System.out.println("User not found in the database.");
        }
    }
}