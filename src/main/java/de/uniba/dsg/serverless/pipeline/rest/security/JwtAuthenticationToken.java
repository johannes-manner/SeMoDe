package de.uniba.dsg.serverless.pipeline.rest.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1234567L;

    private String jwtToken;
    private String username;

    public JwtAuthenticationToken(String jwtToken) {
        super(List.of());
        this.jwtToken = jwtToken;
        this.username = "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getJwtToken() {
        return jwtToken;
    }


    @Override
    public Object getCredentials() {
        return jwtToken;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public String toString() {
        return "JwtAuthenticationToken{" +
                "jwtToken='" + jwtToken + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
