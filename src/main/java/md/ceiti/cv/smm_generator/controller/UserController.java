package md.ceiti.cv.smm_generator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "user/dashboard";
    }

    @GetMapping("/post_generate")
    public String post_generate() {
        return "user/post_generate";
    }
}
