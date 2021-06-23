package de.uniba.dsg.serverless.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping(value = "/login")
public class LoginController {

    @GetMapping
    public String getLoginPage() {
        return "login";
    }
}
