package md.ceiti.cv.smm_generator.controller;

import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import md.ceiti.cv.smm_generator.service.OAuthService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class SocialController {

    private final UserRepository userRepository ;
    private final OAuthService oAuthService;

    public SocialController(UserRepository userRepository, OAuthService oAuthService)
    {
        this.userRepository = userRepository ;
        this.oAuthService = oAuthService ;
    }

    @GetMapping("/user/socials")
    public String showSocialConnections(Model model, Principal principal)
    {
        User user = userRepository.findByEmail(principal.getName());
        model.addAttribute("facebookConnected", user.getFacebookAccessToken() != null);
        model.addAttribute("instagramConnected", user.getInstagramAccessToken() != null);
        return "user/socials";
    }

    @GetMapping("/loginSuccess")
    public String loginSuccess(OAuth2AuthenticationToken authentication){
        oAuthService.saveFacebookToken(authentication);
        return "redirect:/user/socials";
    }

    @GetMapping("/connect/instagram")
    public String connectInstagram(){
        //integr
        return "redirect:/user/socials";
    }

    @PostMapping("/disconnect/facebook")
    public String disconnectFacebook(Principal principal){
        User user = userRepository.findByEmail(principal.getName());
        user.setFacebookAccessToken(null);
        userRepository.save(user);
        return "redirect:/user/socials";
    }

    @PostMapping("/disconnect/instagram")
    public String disconnectInstagram(Principal principal){
        User user = userRepository.findByEmail(principal.getName());
        user.setInstagramAccessToken(null);
        userRepository.save(user);
        return "redirect:/user/socials";
    }
}
