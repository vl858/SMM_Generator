package md.ceiti.cv.smm_generator.service;

import lombok.RequiredArgsConstructor;
import md.ceiti.cv.smm_generator.entity.AiPost;
import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.AiPostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPostGenerationService {

    private final AiPostRepository postRepository;
    private final UserService userService;
    private final FacebookService facebookService;
    private final ImageGenerationService imageGenerationService;

    public List<AiPost> generatePosts(String topic) {
        List<AiPost> posts = new ArrayList<>();
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not found"));

        for (int i = 0; i < 3; i++) {
            String text = generatePostText(topic);
            String hashtags = generateHashtags(topic);
            String imageUrl = imageGenerationService.generateImageUrl(topic);

            AiPost post = AiPost.builder()
                    .text(text)
                    .hashtags(hashtags)
                    .imageUrl(imageUrl)
                    .createdDate(LocalDateTime.now())
                    .published(false)
                    .user(currentUser)
                    .build();

            posts.add(postRepository.save(post));
        }
        return posts;
    }

    private String generatePostText(String topic) {
        return "Check out this amazing post about " + topic + "!";
    }

    private String generateHashtags(String topic) {
        return "#" + topic.toLowerCase().replace(" ", "") + " #trending #aiPost";
    }

    public void updatePost(Long postId, String text, String hashtags, String imageUrl) {
        AiPost post = postRepository.findById(postId).orElseThrow();
        post.setText(text);
        post.setHashtags(hashtags);
        post.setImageUrl(imageUrl);
        postRepository.save(post);
    }

    public void publishPost(Long postId, List<String> platforms, String option, LocalDateTime scheduleDate) {
        AiPost originalPost = postRepository.findById(postId).orElseThrow();

        for (String platform : platforms) {
            AiPost postToPublish = AiPost.builder()
                    .text(originalPost.getText())
                    .hashtags(originalPost.getHashtags())
                    .imageUrl(originalPost.getImageUrl())
                    .platform(platform)
                    .createdDate(LocalDateTime.now())
                    .scheduledDate("schedule".equals(option) ? scheduleDate : null)
                    .published("now".equals(option))
                    .user(originalPost.getUser())
                    .build();

            postRepository.save(postToPublish);

            if ("now".equals(option)) {
                if ("facebook".equalsIgnoreCase(platform)) {
                    facebookService.postToFacebook(postToPublish);
                }
                // You can add future logic for Instagram here
            }
        }
    }

    public List<AiPost> getPostsForCurrentUser() {
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));
        return postRepository.findAllByUser(user);
    }
}