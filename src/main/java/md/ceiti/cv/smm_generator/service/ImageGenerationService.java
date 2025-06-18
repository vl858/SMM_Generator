package md.ceiti.cv.smm_generator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    private final ImageModel imageModel;
    private final Map<String, String> imageCache = new HashMap<>();

    public String generateImageUrl(String topic) {
        if (imageCache.containsKey(topic)) {
            return imageCache.get(topic);
        }

        try {
            ImagePrompt prompt = new ImagePrompt(
                    "Generate an ultra-realistic 4K photo, DSLR quality, vibrant colors, with real people and natural lighting. " +
                            "Create a unique composition based on the topic: \"" + topic + "\". Avoid cartoon or illustration style. No text, no logos."
            );
            ImageResponse response = imageModel.call(prompt);
            String url = response.getResult().getOutput().getUrl();

            imageCache.put(topic, url);
            return url;
        } catch (Exception e) {
            System.out.println("Image generation failed: " + e.getMessage());
            return getFallbackImageUrl();
        }
    }

    private String getFallbackImageUrl() {
        return "/images/placeholder.png";
    }
}