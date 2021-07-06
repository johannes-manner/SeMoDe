package de.uniba.dsg.serverless.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(value = "users")
public class UserController {

    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String getAllUsers(Model model) {

        addModel(model);

        return "users";
    }

    private void addModel(Model model) {
        model.addAttribute("users", this.userService.findAll());
        model.addAttribute("roles", Arrays.stream(Role.values()).map(Role::getRole).collect(Collectors.toList()));
        model.addAttribute("userUpdateForm", new UserUpdateForm());
        model.addAttribute("updatePasswordForm", new PasswordForm());
    }


    @PostMapping(value = "{username}/roles")
    public String changeUserRight(@PathVariable(value = "username") String username, UserUpdateForm formData) {

        log.info("Change user role: " + username + " " + formData.toString());

        User user = this.userService.loadUserByUsername(username);
        if (user == null || formData.getNewRole().isEmpty()) {
            return "redirect:/";
        }

        user.setRole(formData.getNewRole());
        this.userService.save(user);

        return "redirect:/users";
    }

    @PostMapping("{username}/password")
    public String changeUserPassword(Model model,
                                     @PathVariable(value = "username") String username,
                                     @Valid PasswordForm updatePasswordForm, Errors errors) {

        log.info("Change user password: " + username);

        User user = this.userService.loadUserByUsername(username);
        if (user == null || errors.hasErrors()) {
            log.warn("Errors in password form!");
            addModel(model);
            model.addAttribute("errorMessage", "Errors: " +
                    errors.getAllErrors().stream().map(e -> e.getDefaultMessage()).collect(Collectors.joining(", ")));
            return "/users";
        }

        user.setPassword(passwordEncoder.encode(updatePasswordForm.getPassword()));
        this.userService.save(user);

        return "redirect:/users";
    }
}
