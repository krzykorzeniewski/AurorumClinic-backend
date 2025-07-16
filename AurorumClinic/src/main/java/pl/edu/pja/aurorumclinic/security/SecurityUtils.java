package pl.edu.pja.aurorumclinic.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.models.User;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class SecurityUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    public String createJwt(User user) {
        return Jwts.builder()
                .header()
                    .type("JWT")
                .and()
                .claims()
                    .issuer(issuer)
                    .subject(user.getUsername())
                    .add("role", user.getRole().name())
                    .expiration(Date.from(Instant.now().plus(30, ChronoUnit.MINUTES)))
                .and()
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey() {
        byte[] bytes = Utf8.encode(secret);
        return Keys.hmacShaKeyFor(bytes);
    }

    public String getEmailFromJwt(String jwt) {
        Jws<Claims> claims = validateJwt(jwt);
        return claims.getPayload().getSubject();
    }

    public String getRoleFromJwt(String jwt) {
        Jws<Claims> claims = validateJwt(jwt);
        return claims.getPayload().get("role", String.class);
    }

    private Jws<Claims> validateJwt(String jwt) {
        return Jwts.parser()
                .requireIssuer(issuer)
                .clockSkewSeconds(120)
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(jwt);
    }
}
