package pl.edu.pja.aurorumclinic.features.auth.login;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

@RestController
@RequestMapping("/api/auth/login")
@RequiredArgsConstructor
public class LoginHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JwtUtils jwtUtils;
    private final TokenService tokenService;

    @PostMapping
    @Transactional
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginUserRequest requestDto) {
        User userFromDb = userRepository.findByEmail(requestDto.email());
        if (userFromDb == null) {
            throw new ApiAuthException("Invalid credentials", "credentials");
        }
        if (!passwordEncoder.matches(requestDto.password(), userFromDb.getPassword())) {
            throw new ApiAuthException("Invalid credentials", "credentials");
        }
        if (!userFromDb.isEmailVerified()) {
            throw new ApiAuthException("Email is not verified", "email");
        }

        if (userFromDb.isTwoFactorAuth()) {
            applicationEventPublisher.publishEvent(new MfaLoginEvent(userFromDb));
            return ResponseEntity.ok(ApiResponse.success(LoginUserResponse.builder()
                    .userId(userFromDb.getId())
                    .email(userFromDb.getEmail())
                    .twoFactorAuth(userFromDb.isTwoFactorAuth())
                    .role(userFromDb.getRole())
                    .build()));
        }

        String jwt = jwtUtils.createJwt(userFromDb);
        Token refreshToken = tokenService.createToken(userFromDb, TokenName.REFRESH, 60 * 24);

        LoginUserResponse responseDto =  LoginUserResponse.builder()
                .userId(userFromDb.getId())
                .email(userFromDb.getEmail())
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .role(userFromDb.getRole())
                .build();

        if (responseDto.twoFactorAuth()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(responseDto));
        }
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

    public record LoginUserRequest(@NotBlank @Email @Size(max = 100) String email,
                                   @NotBlank @Size(max = 200) String password) {
    }

    @Builder
    public record LoginUserResponse(Long userId,
                                    String email,
                                    boolean twoFactorAuth,
                                    UserRole role) {
    }


}
