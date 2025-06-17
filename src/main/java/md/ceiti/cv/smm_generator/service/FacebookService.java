package md.ceiti.cv.smm_generator.service;

import md.ceiti.cv.smm_generator.entity.AiPost;
import md.ceiti.cv.smm_generator.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FacebookService {

    public void postToFacebook(AiPost post) {
        User user = post.getUser();
        String accessToken = user.getFacebookAccessToken();
        String pageId = user.getFacebookUserId();

        if (accessToken == null || pageId == null) {
            throw new IllegalStateException("Facebook account not connected properly.");
        }

        String url = "https://graph.facebook.com/" + pageId + "/photos";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Creează corpul cererii
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