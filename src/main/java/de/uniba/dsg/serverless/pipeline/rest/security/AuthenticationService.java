package de.uniba.dsg.serverless.pipeline.rest.security;

import de.uniba.dsg.serverless.users.User;
import de.uniba.dsg.serverless.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
public class AuthenticationService {

    private UserRepository userRepository;
    private JwtTokenService jwtTokenService;
    private PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, JwtTokenService jwtTokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public JWTTokenResponse generateJWTToken(String username, String password) {
        User user = userRepository.findUserByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return new JWTTokenResponse(jwtTokenService.generateToken(username));
        } else {
            throw new EntityNotFoundException("User not found");
        }
    }
}
