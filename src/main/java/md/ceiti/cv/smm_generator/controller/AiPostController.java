package md.ceiti.cv.smm_generator.controller;

import lombok.RequiredArgsConstructor;
import md.ceiti.cv.smm_generator.entity.AiPost;
import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import md.ceiti.cv.smm_generator.service.AiPostGenerationService;
import md.ceiti.cv.smm_generator.service.TokenValidatorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AiPostController {

    private final AiPostGenerationService aiPostGenerationService;
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
    public String viewGeneratedPosts(Model model, Principal principal) {
        List<AiPost> posts = aiPostGenerationService.getPostsForCurrentUser();
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        boolean facebookTokenExpired = optionalUser.map(tokenValidatorService::isFacebookTokenExpired).orElse(true);

        model.addAttribute("posts", posts);
        model.addAttribute("facebookTokenExpired", facebookTokenExpired);
        return "user/post_generate";
    }

    @PostMapping("/edit-post")
    public String editPost(@RequestParam Long postId,
                           @RequestParam String text,
                           @RequestParam String hashtags,
                           @RequestParam String imageUrl) {
        aiPostGenerationService.updatePost(postId, text, hashtags, imageUrl);
        return "redirect:/user/post_generate";
    }

    @PostMapping("/publish")
    public String publishPost(@RequestParam Long postId,
                              @RequestParam List<String> platforms,
                              @RequestParam String publishOption,
                              @RequestParam(required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledDate,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {

        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isEmpty() || tokenValidatorService.isFacebookTokenExpired(optionalUser.get())) {
            redirectAttributes.addFlashAttribute("facebookError", "⚠ Facebook token expired. Please reconnect.");
            return "redirect:/reauth/facebook";
        }

        aiPostGenerationService.publishPost(postId, platforms, publishOption, scheduledDate);
        return "redirect:/user/post_generate";
    }
}
