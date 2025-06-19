package md.ceiti.cv.smm_generator.service;

import lombok.RequiredArgsConstructor;
import md.ceiti.cv.smm_generator.entity.AiPost;
import md.ceiti.cv.smm_generator.entity.PostStatus;
import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.AiPostRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AiPostGenerationService {

    private final AiPostRepository postRepository;
    private final UserService userService;
    private final FacebookService facebookService;
    private final ImageGenerationService imageGenerationService;
    private final ChatClient.Builder chatClientBuilder;

    public List<AiPost> generatePosts(String topic) {
        List<AiPost> posts = new ArrayList<>();
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not found"));

        for (int i = 0; i < 3; i++) {
            String text = generatePostText(topic);
            String hashtags = generateHashtags(topic);

            String imagePrompt = "Generate an ultra-realistic photo related to: \"" + topic + "\". " +
                    switch (i) {
                        case 0 -> "Natural lighting, people in real-life settings.";
                        case 1 -> "Real-world scenery, documentary-style composition, authentic background.";
                        case 2 -> "High-resolution candid moment with vibrant colors and emotional context.";
                        default -> "";
                    };

            String imageUrl = imageGenerationService.generateImageUrl(imagePrompt);

            AiPost post = AiPost.builder()
                    .text(text)
                    .hashtags(hashtags)
                    .imageUrl(imageUrl)
                    .createdDate(LocalDateTime.now())
                    .published(null)
                    .status(PostStatus.DRAFT)
                    .user(currentUser)
                    .build();

            posts.add(postRepository.save(post));
        }
        return posts;
    }

    public String generatePostText(String topic) {
        return chatClientBuilder.build()
                .prompt()
                .user("Write a social media post without any hashtags about: " + topic)
                .call()
                .content();
    }

    public String generateHashtags(String topic) {
        return chatClientBuilder.build()
                .prompt()
                .user("Generate 4 relevant hashtags for: " + topic + ". Output as space-separated line.")
                .call()
                .content();
    }

    public void updatePost(Long postId, String text, String hashtags, String imageUrl) {
        AiPost post = postRepository.findById(postId).orElseThrow();
        post.setText(text);
        post.setHashtags(hashtags);
        post.setImageUrl(imageUrl);
        postRepository.save(post);
    }

    public void publishNow(AiPost post, List<String> platforms) {
        for (String platform : platforms) {
            if ("facebook".equalsIgnoreCase(platform)) {
                facebookService.postToFacebook(post);
            }
        }

        post.setPublished(LocalDateTime.now());
        post.setStatus(PostStatus.PUBLISHED);
        postRepository.save(post);
    }

    public void deleteDraftPostsForCurrentUser() {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        postRepository.deleteByUserAndStatus(user, md.ceiti.cv.smm_generator.entity.PostStatus.DRAFT);
    }
}