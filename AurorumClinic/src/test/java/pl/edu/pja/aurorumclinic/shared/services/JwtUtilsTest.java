package pl.edu.pja.aurorumclinic.shared.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.test.context.ActiveProfiles;
import pl.edu.pja.aurorumclinic.shared.JwtUtils;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = {JwtUtils.class})
@ActiveProfiles("test")
public class JwtUtilsTest {

    @Autowired
    JwtUtils jwtUtils;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiration.minutes}")
    private Integer expirationInMinutes;

    @Test
    void createJwtShouldReturnJwtWithCorrectClaimsAndType() {
        User testUser = User.builder()
                .id(1L)
                .role(UserRole.PATIENT)
                .build();

        String result = jwtUtils.createJwt(testUser);

        Jws<Claims> claimsFromJwt = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Utf8.encode(secret)))
                .build()
                .parseSignedClaims(result);

        assertThat(result).isNotNull();
        assertThat(claimsFromJwt.getHeader().getType()).isEqualTo("JWT");
        assertThat(claimsFromJwt.getPayload().getIssuer()).isEqualTo(issuer);
        assertThat(claimsFromJwt.getPayload().getSubject()).isEqualTo(String.valueOf(testUser.getId()));
        assertThat(claimsFromJwt.getPayload().get("role")).isEqualTo(testUser.getRole().name());
        assertThat(claimsFromJwt.getPayload().getExpiration().toInstant())
                .isCloseTo(
                        Instant.now().plus(expirationInMinutes, ChronoUnit.MINUTES),
                        within(5, ChronoUnit.SECONDS)
                );
    }

}
