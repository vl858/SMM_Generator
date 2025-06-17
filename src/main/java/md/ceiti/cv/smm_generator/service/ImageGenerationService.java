package md.ceiti.cv.smm_generator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.image.ImageModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    private final ImageModel imageModel;
    private final Map<String, String> imageCache = new HashMap<>();

    public String generateImageUrl(String topic) {
        if(imageCache.containsKey(topic)) {
            return imageCache.get(topic);
        }
        try {
            ImagePrompt imagePrompt = new ImagePrompt("Generate a realistic image about "+topic);
            ImageResponse imageResponse = imageModel.call(imagePrompt);
            String url = imageResponse.getResult().getOutput().getUrl();

            imageCache.put(topic, url);
            return url;
        }
        catch (Exception e){
            System.out.println("Error generating image " + e.getMessage());
            return getFallBackImageUrl();
        }
    }

    private String getFallBackImageUrl() {
        return "/images/placeholder.png";
    }
}