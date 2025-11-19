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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import pl.edu.pja.aurorumclinic.shared.JwtUtils;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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

    @Test
    void getUserIdFromJwtShouldReturnCorrectUserId() {
        SecretKey secretKey = Keys.hmacShaKeyFor(Utf8.encode(secret));
        User testUser = User.builder()
                .id(1L)
                .role(UserRole.PATIENT)
                .build();
        String testJwt = Jwts.builder()
                .subject(String.valueOf(testUser.getId()))
                .issuer(issuer)
                .signWith(secretKey)
                .compact();

        Long result = jwtUtils.getUserIdFromJwt(testJwt);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testUser.getId());
    }

    @Test
    void getRoleFromJwtShouldReturnCorrectUserRole() {
        SecretKey secretKey = Keys.hmacShaKeyFor(Utf8.encode(secret));
        User testUser = User.builder()
                .id(1L)
                .role(UserRole.PATIENT)
                .build();
        String testJwt = Jwts.builder()
                .subject(String.valueOf(testUser.getId()))
                .claim("role", testUser.getRole().name())
                .issuer(issuer)
                .signWith(secretKey)
                .compact();

        String result = jwtUtils.getRoleFromJwt(testJwt);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testUser.getRole().name());
    }

    @Test
    void getUserIdFromExpiredJwtShouldReturnCorrectUserIdIfJwtIsExpired() {
        SecretKey secretKey = Keys.hmacShaKeyFor(Utf8.encode(secret));
        User testUser = User.builder()
                .id(1L)
                .role(UserRole.PATIENT)
                .build();
        String testJwt = Jwts.builder()
                .subject(String.valueOf(testUser.getId()))
                .issuer(issuer)
                .expiration(Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();

        Long result = jwtUtils.getUserIdFromExpiredJwt(testJwt);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testUser.getId());
    }

}
