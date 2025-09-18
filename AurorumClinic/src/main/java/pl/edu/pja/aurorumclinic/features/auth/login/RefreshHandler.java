package pl.edu.pja.aurorumclinic.features.auth.login;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthException;
import pl.edu.pja.aurorumclinic.features.auth.shared.JwtUtils;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth/refresh")
@RequiredArgsConstructor
public class RefreshHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping
    public ResponseEntity<?> refreshAccessToken(@CookieValue("Access-Token") String accessToken,
                                                @CookieValue("Refresh-Token") String refreshToken) {
        @Valid RefreshAccessTokenRequest requestDto = new RefreshAccessTokenRequest(accessToken, refreshToken);
        String jwt = requestDto.accessToken();
        Long userId;
        try {
            jwtUtils.validateJwt(jwt);
            userId = jwtUtils.getUserIdFromJwt(jwt);
        } catch (ExpiredJwtException e) {
            userId = jwtUtils.getUserIdFromExpiredJwt(jwt);
        } catch (JwtException jwtException) {
            throw new ApiAuthException(jwtException.getMessage(), "accessToken");
        }

        User userFromDb = userRepository.findById(userId).orElseThrow(
                () -> new ApiAuthException("Invalid credentials", "credentials")
        );
        Token token = userFromDb.getTokens().stream()
                .filter(t -> passwordEncoder.matches(requestDto.refreshToken, t.getValue()))
                .findFirst().orElseThrow(() -> new ApiAuthException("Invalid token", "token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiAuthException("Refresh token is expired", "refreshToken");
        }

        String newJwt = jwtUtils.createJwt(userFromDb);
        Token newRefreshToken = tokenService.createToken(userFromDb, TokenName.REFRESH, 60 * 24);;

        RefreshAccessTokenResponse responseDto = RefreshAccessTokenResponse.builder()
                .userId(userFromDb.getId())
                .email(userFromDb.getEmail())
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .role(userFromDb.getRole())
                .build();

        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", newJwt)
                .path("/")
                .httpOnly(true)
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", newRefreshToken.getRawValue())
                .path("/")
                .httpOnly(true)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(responseDto));
    }

    public record RefreshAccessTokenRequest(@NotNull @Size(max = 1000) String accessToken,
                                            @NotNull @Size(max = 200) String refreshToken) {
    }

    @Builder
    public record RefreshAccessTokenResponse(Long userId,
                                             String email,
                                             boolean twoFactorAuth,
                                             UserRole role) {
    }

}
