package md.ceiti.cv.smm_generator.controller;

import lombok.RequiredArgsConstructor;
import md.ceiti.cv.smm_generator.entity.AiPost;
import md.ceiti.cv.smm_generator.entity.PostStatus;
import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.AiPostRepository;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import md.ceiti.cv.smm_generator.service.AiPostGenerationService;
import md.ceiti.cv.smm_generator.service.TokenValidatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class AiPostController {

    private final AiPostGenerationService aiPostGenerationService;
    private final AiPostRepository aiPostRepository;
    private final UserRepository userRepository;
    private final TokenValidatorService tokenValidatorService;

    @PostMapping("/generate-posts")
    public String generatePosts(@RequestParam String topic,
                                Model model,
                                Principal principal) {
        List<AiPost> posts = aiPostGenerationService.generatePosts(topic);
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        boolean facebookTokenExpired = optionalUser.map(tokenValidatorService::isFacebookTokenExpired).orElse(true);

        model.addAttribute("posts", posts);
        model.addAttribute("facebookTokenExpired", facebookTokenExpired);
        return "user/post_generate";
    }

    @GetMapping("/user/post_generate")
    public String viewPostGeneratorPage(Model model, Principal principal) {
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        boolean facebookTokenExpired = optionalUser.map(tokenValidatorService::isFacebookTokenExpired).orElse(true);

        model.addAttribute("facebookTokenExpired", facebookTokenExpired);
        return "user/post_generate";
    }

    @PostMapping("/edit-post")
    @ResponseBody
    public ResponseEntity<Void> editPost(@RequestParam Long postId,
                                         @RequestParam String text,
                                         @RequestParam String hashtags,
                                         @RequestParam String imageUrl) {
        aiPostGenerationService.updatePost(postId, text, hashtags, imageUrl);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/publish")
    public String publishNow(@RequestParam Long postId,
                             @RequestParam(required = false) String[] platforms,
                             RedirectAttributes redirectAttributes) {
        AiPost post = aiPostRepository.findById(postId).orElseThrow();
        post.setStatus(PostStatus.PUBLISHED);

        if (platforms != null) {
            List<String> filteredPlatforms = Arrays.stream(platforms)
                    .filter(p -> p.equalsIgnoreCase("facebook"))
                    .toList();

            post.setPlatform(String.join(",", filteredPlatforms));
            aiPostRepository.save(post);

            if (!filteredPlatforms.isEmpty()) {
                aiPostGenerationService.publishNow(post, filteredPlatforms);
            }
        } else {
            post.setPlatform(null);
            aiPostRepository.save(post);
        }

        redirectAttributes.addFlashAttribute("message", "Post published successfully.");
        redirectAttributes.addFlashAttribute("alertType", "success");
        return "redirect:/user/post_generate";
    }

    @GetMapping("/generate")
    public String clearDraftsAndRedirect() {
        aiPostGenerationService.deleteDraftPostsForCurrentUser();
        return "redirect:/user/post_generate";
    }

    @GetMapping("/user/calendar")
    public String showCalendar(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        List<AiPost> posts = aiPostRepository.findByUserAndStatus(user, PostStatus.PUBLISHED);

        List<Map<String, Object>> calendarPosts = posts.stream().map(post -> {
            Map<String, Object> event = new HashMap<>();
            event.put("title", post.getText());
            event.put("start", post.getPublished().toString());
            return event;
        }).toList();

        model.addAttribute("calendarPosts", calendarPosts);
        return "user/post_calendar";
    }
}