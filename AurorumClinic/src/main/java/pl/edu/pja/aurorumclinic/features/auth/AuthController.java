package pl.edu.pja.aurorumclinic.features.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.auth.dtos.request.*;
import pl.edu.pja.aurorumclinic.features.auth.dtos.response.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-employee")
    public ResponseEntity<?> registerEmployee(@Valid @RequestBody RegisterEmployeeRequest requestDto) {
        authService.registerEmployee(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @PostMapping("/register-patient")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody RegisterPatientRequest requestDto) {
        authService.registerPatient(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @PostMapping("/register-doctor")
    public ResponseEntity<?> registerDoctor(@Valid @RequestBody RegisterDoctorRequest requestDto) {
        authService.registerDoctor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @PostMapping("/verify-email-token")
    public ResponseEntity<?> getVerifyEmailToken(@Valid @RequestBody VerifyEmailTokenRequest requestDto) {
        authService.sendVerifyUserAccountEmail(requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest requestDto) {
        authService.verifyUserEmail(requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/verify-phone-number-token")
    public ResponseEntity<?> getVerifyPhoneNumberToken(@Valid @RequestBody VerifyPhoneNumberTokenRequest requestDto) {
        authService.sendVerifyPhoneNumberMessage(requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/verify-phone-number")
    public ResponseEntity<?> verifyPhoneNumber(@Valid @RequestBody VerifyPhoneNumberRequest requestDto) {
        authService.verifyPhoneNumber(requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reset-password-token")
    public ResponseEntity<?> getResetPasswordToken(@Valid @RequestBody PasswordResetTokenRequest requestDto) {
        authService.sendResetPasswordEmail(requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest requestDto) {
        authService.resetPassword(requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginUserRequest requestDto) {
        LoginUserResponse responseDto = authService.loginUser(requestDto);
        if (responseDto.twoFactorAuth()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(responseDto));
        }
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", responseDto.accessToken())
                .path("/")
                .httpOnly(true)
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", responseDto.refreshToken())
                .path("/")
                .httpOnly(true)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(responseDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue("Access-Token") String accessToken,
                                                @CookieValue("Refresh-Token") String refreshToken) {
        @Valid RefreshAccessTokenRequest requestDto = new RefreshAccessTokenRequest(accessToken, refreshToken);
        RefreshAccessTokenResponse responseDto = authService.refreshAccessToken(requestDto);
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", responseDto.accessToken())
                .path("/")
                .httpOnly(true)
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", responseDto.refreshToken())
                .path("/")
                .httpOnly(true)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(responseDto));
    }

    @PostMapping("/login-2fa")
    public ResponseEntity<?> loginUserWith2fa(@Valid @RequestBody TwoFactorAuthLoginRequest requestDto) {
        TwoFactorAuthLoginResponse responseDto = authService.loginUserWithTwoFactorAuth(requestDto);
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", responseDto.accessToken())
                .path("/")
                .httpOnly(true)
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", responseDto.refreshToken())
                .path("/")
                .httpOnly(true)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(responseDto));
    }

    @PostMapping("/2fa-token")
    public ResponseEntity<?> get2faToken(@Valid @RequestBody TwoFactorAuthTokenRequest twoFactorAuthTokenRequest) {
        authService.send2faToken(twoFactorAuthTokenRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(null));
    }

    @GetMapping("/basic-info")
    public ResponseEntity<?> getBasicUserInfo(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(authService.getBasicUserInfo(authentication)));
    }

}
