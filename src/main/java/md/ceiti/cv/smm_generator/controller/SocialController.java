package md.ceiti.cv.smm_generator.controller;

import lombok.RequiredArgsConstructor;
import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import md.ceiti.cv.smm_generator.service.FacebookOAuthHelper;
import md.ceiti.cv.smm_generator.service.TokenValidatorService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class SocialController {

    private final UserRepository userRepository;
    private final FacebookOAuthHelper facebookOAuthHelper;
    private final TokenValidatorService tokenValidatorService;

    @GetMapping("/user/socials")
    public String showSocialConnections(Model model, Principal principal) {
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isEmpty()) return "redirect:/login";

        User user = optionalUser.get();
        boolean facebookConnected = user.getFacebookPageToken() != null;
        boolean facebookExpired = facebookConnected && tokenValidatorService.isFacebookTokenExpired(user);

        model.addAttribute("facebookConnected", facebookConnected);
        model.addAttribute("facebookTokenExpired", facebookExpired);

        return "user/socials";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(OAuth2AuthenticationToken authentication, RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getPrincipal().getAttribute("email");
            facebookOAuthHelper.handleFacebookLogin(authentication, email);
            redirectAttributes.addFlashAttribute("facebookSuccess", "✅ Facebook connected successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("facebookError", "❌ Facebook connection failed.");
        }
        return "redirect:/user/socials";
    }

    @GetMapping("/reauth/facebook")
    public String reconnectFacebook() {
        return "redirect:/oauth2/authorization/facebook";
    }

    @GetMapping("/disconnect/facebook")
    public String disconnectFacebook(Principal principal, RedirectAttributes redirectAttributes) {
        userRepository.findByEmail(principal.getName()).ifPresent(user -> {
            user.setFacebookPageToken(null);
            user.setFacebookPageId(null);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("facebookSuccess", "🔌 Facebook disconnected.");
        });
        return "redirect:/user/socials";
    }
}
