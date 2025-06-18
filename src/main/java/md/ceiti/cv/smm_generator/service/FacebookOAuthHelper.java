package md.ceiti.cv.smm_generator.service;

import lombok.RequiredArgsConstructor;
import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacebookOAuthHelper {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserRepository userRepository;

    @Value("${facebook.app.id}")
    private String appId;

    @Value("${facebook.app.secret}")
    private String appSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public void handleFacebookLogin(OAuth2AuthenticationToken authentication, String currentUsername) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());

        if (client == null || client.getAccessToken() == null) {
            System.out.println("Failed to retrieve access token from Facebook.");
            return;
        }

        String shortLivedToken = client.getAccessToken().getTokenValue();
        String longLivedToken = exchangeForLongLivedToken(shortLivedToken);

        if (longLivedToken != null) {
            Optional<User> userOpt = userRepository.findByEmail(currentUsername);
            if (userOpt.isEmpty()) return;

            User user = userOpt.get();
            user.setFacebookAccessToken(longLivedToken);

            LocalDateTime realExpiry = getRealTokenExpiry(longLivedToken);
            user.setFacebookTokenExpiry(realExpiry != null ? realExpiry : LocalDateTime.now().plusDays(60));

            userRepository.save(user);

            Collection<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> (GrantedAuthority) role::getName)
                    .collect(Collectors.toList());

            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    user.getEmail(), user.getPassword(), authorities);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    public String exchangeForLongLivedToken(String shortToken) {
        String url = "https://graph.facebook.com/v19.0/oauth/access_token" +
                "?grant_type=fb_exchange_token" +
                "&client_id=" + appId +
                "&client_secret=" + appSecret +
                "&fb_exchange_token=" + shortToken;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            return body != null ? (String) body.get("access_token") : null;
        } catch (Exception e) {
            System.out.println("Error exchanging token: " + e.getMessage());
            return null;
        }
    }

    public LocalDateTime getRealTokenExpiry(String userAccessToken) {
        String debugUrl = "https://graph.facebook.com/debug_token" +
                "?input_token=" + userAccessToken +
                "&access_token=" + appId + "|" + appSecret;

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(debugUrl, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data.containsKey("expires_at")) {
                    long expiresAt = ((Number) data.get("expires_at")).longValue();
                    return Instant.ofEpochSecond(expiresAt).atZone(ZoneId.systemDefault()).toLocalDateTime();
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch real expiry: " + e.getMessage());
        }
        return null;
    }
}