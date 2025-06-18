package md.ceiti.cv.smm_generator.controller;

import md.ceiti.cv.smm_generator.dto.UserDto;
import md.ceiti.cv.smm_generator.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "email") String sort,
            Model model) {

        Page<UserDto> usersPage = userService.findPaginatedSorted(page, size, sort);
        model.addAttribute("usersPage", usersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("sort", sort);

        return "admin/dashboard";
    }
}