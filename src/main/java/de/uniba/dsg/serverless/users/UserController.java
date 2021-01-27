package de.uniba.dsg.serverless.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

@Slf4j
@Controller
@RequestMapping(value = "users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getAllUsers(Model model) {

        model.addAttribute("users", this.userService.findAll());
        model.addAttribute("roles", Arrays.asList("ROLE_ADMIN", "ROLE_USER"));
        model.addAttribute("userUpdateForm", new UserUpdateForm());

        return "users";
    }

    @PostMapping(value = "{username}/roles")
    public String changeUserRight(@PathVariable(value = "username") String username, UserUpdateForm formData) {

        log.info("Change user: " + username + " " + formData.toString());

        User user = this.userService.loadUserByUsername(username);
        if (user == null || formData.getNewRole().isEmpty()) {
            return "redirect:/";
        }

        user.setRole(formData.getNewRole());
        this.userService.save(user);

        return "redirect:/users";
    }
}
