package md.ceiti.cv.smm_generator.service;

import md.ceiti.cv.smm_generator.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TokenValidatorService {
    public boolean isFacebookTokenExpired(User user) {
        String token = user.getFacebookAccessToken();
        if (token == null) return true;

        try {
            String url = "https://graph.facebook.com/me?access_token=" + token;
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForObject(url, String.class);
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}