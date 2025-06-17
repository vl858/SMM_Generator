package md.ceiti.cv.smm_generator.controller;

import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import md.ceiti.cv.smm_generator.service.FacebookOAuthHelper;
import md.ceiti.cv.smm_generator.service.InstagramService;
import md.ceiti.cv.smm_generator.service.TokenValidatorService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
public class SocialController {

    private final UserRepository userRepository;
    private final InstagramService instagramService;
    private final FacebookOAuthHelper facebookOAuthHelper;
    private final TokenValidatorService tokenValidatorService;

    public SocialController(UserRepository userRepository,
                            InstagramService instagramService,
                            FacebookOAuthHelper facebookOAuthHelper,
                            TokenValidatorService tokenValidatorService) {
        this.userRepository = userRepository;
        this.instagramService = instagramService;
        this.facebookOAuthHelper = facebookOAuthHelper;
        this.tokenValidatorService = tokenValidatorService;
    }

    @GetMapping("/user/socials")
    public String showSocialConnections(Model model, Principal principal) {
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isEmpty()) return "redirect:/login";

        User user = optionalUser.get();
        boolean facebookConnected = user.getFacebookAccessToken() != null;
        boolean facebookTokenExpired = tokenValidatorService.isFacebookTokenExpired(user);

        model.addAttribute("facebookConnected", facebookConnected);
        model.addAttribute("facebookTokenExpired", facebookConnected && facebookTokenExpired);
        model.addAttribute("instagramConnected", user.getInstagramAccessToken() != null);

        return "user/socials";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(OAuth2AuthenticationToken authentication, RedirectAttributes redirectAttributes) {
        try {
            facebookOAuthHelper.handleFacebookLogin(authentication);
            redirectAttributes.addFlashAttribute("facebookSuccess", "✅ Facebook connected successfully with long-lived token.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("facebookError", "⚠ Failed to connect Facebook.");
        }
        return "redirect:/user/socials";
    }

    @GetMapping("/reauth/facebook")
    public String reconnectFacebook() {
        return "redirect:/oauth2/authorization/facebook";
    }

    @GetMapping("/connect/instagram")
    public String connectInstagram(Principal principal, RedirectAttributes redirectAttributes) {
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isEmpty()) return "redirect:/login";

        User user = optionalUser.get();
        boolean connected = instagramService.connectInstagram(user);

        if (connected) {
            redirectAttributes.addFlashAttribute("instagramSuccess", "✅ Instagram account connected successfully.");
        } else {
            redirectAttributes.addFlashAttribute("instagramError", "⚠ No Instagram Business account was found linked to your Facebook pages.");
        }
        return "redirect:/user/socials";
    }

    @PostMapping("/disconnect/facebook")
    public String disconnectFacebook(Principal principal, RedirectAttributes redirectAttributes) {
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isEmpty()) return "redirect:/login";

        User user = optionalUser.get();
        user.setFacebookAccessToken(null);
        user.setFacebookUserId(null);
        user.setFacebookTokenExpiry(null);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("facebookSuccess", "✅ Facebook account disconnected.");
        return "redirect:/user/socials";
    }

    @PostMapping("/disconnect/instagram")
    public String disconnectInstagram(Principal principal, RedirectAttributes redirectAttributes) {
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isEmpty()) return "redirect:/login";

        User user = optionalUser.get();
        user.setInstagramAccessToken(null);
        user.setInstagramUserId(null);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("instagramSuccess", "✅ Instagram account disconnected.");
        return "redirect:/user/socials";
    }
}