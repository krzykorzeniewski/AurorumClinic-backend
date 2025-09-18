package pl.edu.pja.aurorumclinic.features.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.shared.data.models.User;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtUtils {

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
                    .subject(String.valueOf(user.getId()))
                    .add("role", user.getRole().name())
                    .expiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .and()
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey() {
        byte[] bytes = Utf8.encode(secret);
        return Keys.hmacShaKeyFor(bytes);
    }

    public Long getUserIdFromJwt(String jwt) {
        Jws<Claims> claims = validateJwt(jwt);
        return Long.valueOf(claims.getPayload().getSubject());
    }

    public String getRoleFromJwt(String jwt) {
        Jws<Claims> claims = validateJwt(jwt);
        return claims.getPayload().get("role", String.class);
    }

    public Jws<Claims> validateJwt(String jwt) {
        return Jwts.parser()
                .requireIssuer(issuer)
                .clockSkewSeconds(120)
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(jwt);
    }

    public Long getUserIdFromExpiredJwt(String jwt) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .requireIssuer(issuer)
                .clockSkewSeconds(120)
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(jwt);
            return Long.valueOf(claims.getPayload().getSubject());
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            return Long.valueOf(claims.getSubject());
        }
    }
}
