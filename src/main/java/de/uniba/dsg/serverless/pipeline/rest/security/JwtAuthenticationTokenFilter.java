package de.uniba.dsg.serverless.pipeline.rest.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String requestHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            String authToken = requestHeader.substring(7);
            log.debug("Bearer token: " + authToken);
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(authToken));
        }
        try {
            chain.doFilter(request, response);
        } catch (JwtTokenInvalidException ex) {
            log.warn("Jwt error!");
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            response.getWriter().write(ex.getMessage());
        }
    }
}