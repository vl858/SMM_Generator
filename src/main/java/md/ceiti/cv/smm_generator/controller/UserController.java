package md.ceiti.cv.smm_generator.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "user/dashboard";
    }
//
//    @GetMapping("/post_generate")
//    public String post_generate() {
//        return "user/post_generate";
//    }

    @GetMapping("/calendar")
    public String calendar() {
        return "user/calendar";
    }

    @GetMapping("/settings")
    public String settings(Model model, Principal principal) {
        String email = principal.getName();
        Optional<User> optionalUser = userService.findUserByEmail(email);

        if (optionalUser.isEmpty()) {
            return "redirect:/login?error=userNotFound";
        }

        User user = optionalUser.get();
        String formattedDate = user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        model.addAttribute("profile", user);
        model.addAttribute("date", formattedDate);
        return "user/settings";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal,
                                 Model model) {
        String email = principal.getName();
        Optional<User> optionalUser = userService.findUserByEmail(email);

        if (optionalUser.isEmpty()) {
            model.addAttribute("error", "User not found.");
            return "user/settings";
        }

        User user = optionalUser.get();

        if (userService.verifyPassword(currentPassword, user.getPassword())) {
            userService.updatePassword(email, newPassword);
            model.addAttribute("message", "Password changed successfully!");
        } else {
            model.addAttribute("error", "Current password is incorrect.");
        }

        model.addAttribute("profile", user);
        return "user/settings";
    }

    @PostMapping("/delete")
    public String deleteAccount(HttpServletRequest request, Principal principal) {
        String email = principal.getName();
        userService.deleteUser(email);

        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();

        return "redirect:/login?accountDeleted";
    }
}
