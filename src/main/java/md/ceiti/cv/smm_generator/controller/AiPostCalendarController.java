package md.ceiti.cv.smm_generator.controller;

import lombok.RequiredArgsConstructor;
import md.ceiti.cv.smm_generator.entity.AiPost;
import md.ceiti.cv.smm_generator.entity.PostStatus;
import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.AiPostRepository;
import md.ceiti.cv.smm_generator.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class AiPostCalendarController {

    private final AiPostRepository postRepository;
    private final UserService userService;

    @GetMapping("/calendar/events")
    public ResponseEntity<List<Map<String, Object>>> getCalendarEvents() {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        List<AiPost> posts = postRepository.findByUserAndStatus(currentUser, PostStatus.PUBLISHED);

        List<Map<String, Object>> events = new ArrayList<>();

        for (AiPost post : posts) {
            Map<String, Object> extendedProps = new HashMap<>();
            extendedProps.put("hashtags", post.getHashtags());
            extendedProps.put("platform", post.getPlatform());

            Map<String, Object> event = new HashMap<>();
            event.put("title", post.getText());
            event.put("start", post.getPublished());
            event.put("extendedProps", extendedProps);

            events.add(event);
        }

        return ResponseEntity.ok(events);
    }
}