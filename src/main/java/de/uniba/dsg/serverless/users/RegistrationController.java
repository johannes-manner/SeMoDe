package de.uniba.dsg.serverless.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Slf4j
@Controller
@RequestMapping(value = "/register")
public class RegistrationController {

    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String getRegistrationForm(Model model) {

        log.info("Show registration page");
        model.addAttribute("registrationForm", new RegistrationForm());

        return "register";
    }

    @PostMapping
    public String createUser(@Valid RegistrationForm registrationForm, Errors errors) {

        if (errors.hasErrors()) {
            log.info("User registration contained errors: " + registrationForm.toString());
            return "register";
        }

        // first user is admin
        if (this.userService.count() == 0) {
            this.userService.save(registrationForm.toUser(this.passwordEncoder, Role.ADMIN.getRole()));
        } else {
            this.userService.save(registrationForm.toUser(this.passwordEncoder, Role.USER.getRole()));
        }

        return "login";
    }
}
