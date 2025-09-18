package pl.edu.pja.aurorumclinic.features.auth.login;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@RequestMapping("/api/auth/login-mfa")
@RequiredArgsConstructor
public class MfaLoginHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final TokenService tokenService;

    @PostMapping("/login-2fa")
    @Transactional
    public ResponseEntity<?> loginUserWith2fa(@Valid @RequestBody TwoFactorAuthLoginRequest requestDto) {
        User userFromDb = userRepository.findById(requestDto.userId()).orElseThrow(
                () -> new ApiAuthException("User not found", "id")
        );
        Token token = userFromDb.getTokens().stream()
                .filter(t -> passwordEncoder.matches(requestDto.token(), t.getValue()))
                .findFirst().orElseThrow(() -> new ApiAuthException("Invalid token", "token"));
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiAuthException("Code is expired", "otp");
        }

        String jwt = jwtUtils.createJwt(userFromDb);
        Token refreshToken = tokenService.createToken(userFromDb, TokenName.REFRESH, 60 * 24);

        TwoFactorAuthLoginResponse responseDto = TwoFactorAuthLoginResponse.builder()
                .userId(userFromDb.getId())
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .role(userFromDb.getRole())
                .build();

        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", jwt)
                .path("/")
                .httpOnly(true)
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", refreshToken.getRawValue())
                .path("/")
                .httpOnly(true)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(responseDto));
    }

    public record TwoFactorAuthLoginRequest(@NotBlank @Size(min = 6, max = 6) String token,
                                            @NotNull Long userId) {
    }

    @Builder
    public record TwoFactorAuthLoginResponse(Long userId,
                                             boolean twoFactorAuth,
                                             UserRole role) {
    }

}
