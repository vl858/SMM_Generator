package md.ceiti.cv.smm_generator.service;

import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class InstagramService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public InstagramService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean connectInstagram(User user) {
        String userToken = user.getFacebookAccessToken();
        if (userToken == null) return false;

        String url = "https://graph.facebook.com/v18.0/me/account?access_token=" + userToken;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        List<Map<String, Object>> pages = (List<Map<String, Object>>) response.getBody().get("data");

        for (Map<String, Object> page : pages) {
            String pageId = (String) page.get("id");
            String pageAccessToken = (String) page.get("access_token");

            String igUrl = "https://graph.facebook.com/v18.0/" + pageId +
                    "?fields=instagram_business_account&access_token=" + pageAccessToken;

            ResponseEntity<Map> igResponse = restTemplate.getForEntity(igUrl, Map.class);
            Map<String, Object> igData = (Map<String, Object>) igResponse.getBody().get("instagram_business_account");

            if(igData != null && igData.containsKey("id")) {
                String instagramUserId = (String) igData.get("id");

                user.setInstagramUserId(instagramUserId);
                user.setInstagramAccessToken(pageAccessToken);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}
