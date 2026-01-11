package pl.edu.pja.aurorumclinic.features.auth.login;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.auth.login.dtos.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@PermitAll
@RateLimiting(name = "sensitive")
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginUserResponse>> loginUser(@Valid @RequestBody LoginUserRequest requestDto) {
        LoginUserResponse responseDto = loginService.login(requestDto);
        if (responseDto.twoFactorAuth()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(responseDto));
        }
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", responseDto.accessToken())
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", responseDto.refreshToken())
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(responseDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginUserResponse>> refreshAccessToken(@CookieValue("Access-Token") String accessToken,
                                                @CookieValue("Refresh-Token") String refreshToken) {
        @Valid RefreshAccessTokenRequest requestDto = new RefreshAccessTokenRequest(accessToken, refreshToken);
        LoginUserResponse responseDto = loginService.refresh(requestDto);
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", responseDto.accessToken())
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", responseDto.refreshToken())
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(responseDto));
    }

    @PostMapping("/login-2fa")
    public ResponseEntity<ApiResponse<LoginUserResponse>> loginUserWith2fa(@Valid @RequestBody TwoFactorAuthLoginRequest requestDto) {
        LoginUserResponse responseDto = loginService.login2fa(requestDto);
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", responseDto.accessToken())
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", responseDto.refreshToken())
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(responseDto));
    }

    @PostMapping("/login-2fa-token")
    public ResponseEntity<ApiResponse<?>> get2faToken(@Valid @RequestBody TwoFactorAuthTokenRequest twoFactorAuthTokenRequest) {
        loginService.createMfaToken(twoFactorAuthTokenRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logoutUser() {
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .secure(true)
                .sameSite("None")
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .secure(true)
                .sameSite("None")
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(null));
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

}
