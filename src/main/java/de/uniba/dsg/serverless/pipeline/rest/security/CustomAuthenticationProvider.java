package de.uniba.dsg.serverless.pipeline.rest.security;

import de.uniba.dsg.serverless.users.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
public class CustomAuthenticationProvider implements AuthenticationProvider {

    // delegate the authentication to the DaoAuthenticationProvider built in with Spring Boot
    // the authentication provider uses the user details service and password encoder as delegates
    private DaoAuthenticationProvider daoAuthenticationProvider;

    private final JwtTokenService tokenService;

    @Autowired
    public CustomAuthenticationProvider(PasswordEncoder passwordEncoder, UserService userDetailsService, JwtTokenService tokenService) {
        daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);

        this.tokenService = tokenService;
    }

    /**
     * Use the authenticated method in {@link de.uniba.dsg.serverless.users.SecurityConfig} to authenticate also
     * the rest API methods.
     *
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException, ExpiredJwtException {
        // spring security web & thymeleaf only accept UsernamePasswordAuthenticationTokens
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            return daoAuthenticationProvider.authenticate(authentication);
        } else if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
            try {
                String username = tokenService.getUsernameFromToken(jwtAuthenticationToken.getJwtToken());

                log.info("User accessed the REST API: " + username);
                if (tokenService.validateToken(jwtAuthenticationToken.getJwtToken())) {
                    jwtAuthenticationToken.setAuthenticated(true);
                } else {
                    throw new JwtTokenInvalidException("Token errors.");
                }
            } catch (ExpiredJwtException e) {
                throw e;
            } catch (JwtException e) {
                throw new JwtTokenInvalidException("Token errors.");
            }
            return jwtAuthenticationToken;
        } else {
            throw new IllegalArgumentException("Authentication class not supported ...");
        }
    }

    /**
     * {@link UsernamePasswordAuthenticationToken} is returned by the {@link org.springframework.security.core.userdetails.UserDetailsService}
     * which is registered in the {@link de.uniba.dsg.serverless.users.SecurityConfig}.
     *
     * @param authentication
     * @return
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class)
                || authentication.equals(JwtAuthenticationToken.class);
    }
}
