package pl.edu.pja.aurorumclinic.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.users.dtos.*;
import pl.edu.pja.aurorumclinic.users.services.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register-employee")
    public ResponseEntity<String> registerEmployee(@Valid @RequestBody RegisterEmployeeRequest requestDto) {
        userService.registerEmployee(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/register-patient")
    public ResponseEntity<String> registerPatient(@Valid @RequestBody RegisterPatientRequest requestDto) {
        userService.registerPatient(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/register-doctor")
    public ResponseEntity<String> registerDoctor(@Valid @RequestBody RegisterDoctorRequest requestDto) {
        userService.registerDoctor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/verify-email-token")
    public ResponseEntity<?> getVerifyEmailToken(@Valid @RequestBody VerifyEmailTokenRequest requestDto) {
        userService.sendVerifyUserAccountEmail(requestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        userService.verifyUserEmail(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/reset-password-token")
    public ResponseEntity<?> getResetPasswordToken(@Valid @RequestBody PasswordResetTokenRequest requestDto) {
        userService.sendResetPasswordEmail(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body("Reset password email has been sent if the account is valid");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest requestDto) {
        userService.resetPassword(requestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginUserRequest requestDto) {
        AccessToken responseDto = userService.loginUser(requestDto);
        if (responseDto.twoFactorAuth()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(UserIdResponse.builder()
                            .userId(responseDto.userId())
                            .twoFactorAuth(responseDto.twoFactorAuth())
                            .build());
        }
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", responseDto.accessToken())
                .path("/")
                .httpOnly(true)
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", responseDto.refreshToken())
                .path("/")
                .httpOnly(true)
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(new UserIdResponse(responseDto.userId(), false));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue("Access-Token") String accessToken,
                                                   @CookieValue("Refresh-Token") String refreshToken) {
        @Valid RefreshTokenRequest requestDto = new RefreshTokenRequest(accessToken, refreshToken);
        AccessToken responseDto = userService.refreshAccessToken(requestDto);
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", responseDto.accessToken())
                .path("/")
                .httpOnly(true)
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", responseDto.refreshToken())
                .path("/")
                .httpOnly(true)
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(new UserIdResponse(responseDto.userId(), false));
    }

    @PostMapping("/login-2fa")
    public ResponseEntity<?> loginUserWith2fa(@Valid @RequestBody TwoFactorAuthLoginRequest requestDto) {
        AccessToken responseDto = userService.loginUserWithTwoFactorAuth(requestDto);
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", responseDto.accessToken())
                .path("/")
                .httpOnly(true)
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", responseDto.refreshToken())
                .path("/")
                .httpOnly(true)
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(new UserIdResponse(responseDto.userId(), true));
    }

    @PostMapping("/2fa-token")
    public ResponseEntity<?> get2faToken(@Valid @RequestBody TwoFactorAuthTokenRequest twoFactorAuthTokenRequest) {
        userService.send2faToken(twoFactorAuthTokenRequest);
        return ResponseEntity.ok().build();
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
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .build();
    }

}
