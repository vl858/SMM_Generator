package md.ceiti.cv.smm_generator.service;

import md.ceiti.cv.smm_generator.entity.AiPost;
import md.ceiti.cv.smm_generator.entity.User;
import lombok.RequiredArgsConstructor;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FacebookService {

    private final UserRepository userRepository;
    private final FacebookOAuthHelper facebookOAuthHelper;

    public void postToFacebook(AiPost post) {
        User user = post.getUser();

        if (user.getFacebookTokenExpiry() != null && user.getFacebookTokenExpiry().isBefore(LocalDateTime.now())) {
            String refreshedToken = facebookOAuthHelper.exchangeForLongLivedToken(user.getFacebookPageToken());
            if (refreshedToken != null) {
                user.setFacebookPageToken(refreshedToken);

                LocalDateTime realExpiry = facebookOAuthHelper.getRealTokenExpiry(refreshedToken);
                user.setFacebookTokenExpiry(Objects.requireNonNullElseGet(realExpiry, () -> LocalDateTime.now().plusDays(60)));

                userRepository.save(user);
            } else {
                throw new RuntimeException("Failed to refresh Facebook access token.");
            }
        }

        String accessToken = user.getFacebookPageToken();
        String pageId = user.getFacebookPageId();

        if (accessToken == null || pageId == null) {
            System.out.println("Facebook page token or ID is null");
            System.out.println("Token: " + accessToken);
            System.out.println("Page ID: " + pageId);
            throw new IllegalStateException("Facebook account not connected properly. Please reconnect your account.");
        }

        String url = "https://graph.facebook.com/" + pageId + "/photos";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("url", post.getImageUrl());
        body.add("caption", post.getText() + "\n" + post.getHashtags());
        body.add("access_token", accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Facebook post failed: " + response.getBody());
        }
    }
}