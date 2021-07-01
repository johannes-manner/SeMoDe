package de.uniba.dsg.serverless.pipeline.rest.security;

import de.uniba.dsg.serverless.users.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Component
public class JwtTokenService {

    private String secret;

    private Long expiration;

    public JwtTokenService(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.expiration}") Long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    public String generateToken(String username, String role) {
        final Date createdDate = new Date();
        final Date expirationDate = calculateExpirationDate(createdDate);

        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setIssuer(username)
                .setSubject(role)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // used for token validation
    private Date calculateExpirationDate(Date createdDate) {
        return new Date(createdDate.getTime() + expiration);
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuer);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public Role getRoleFromToken(String token) throws IllegalArgumentException {
        String role = getClaimFromToken(token, Claims::getSubject);
        return Role.valueOf(role);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        return isTokenNotExpired(token) && Role.isValidRole(getClaimFromToken(token, Claims::getSubject));
    }

    private Boolean isTokenNotExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.after(new Date());
    }
}
