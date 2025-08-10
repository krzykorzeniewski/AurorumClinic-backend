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
    public ResponseEntity<String> registerEmployee(@Valid @RequestBody RegisterEmployeeRequestDto requestDto) {
        userService.registerEmployee(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/register-patient")
    public ResponseEntity<String> registerPatient(@Valid @RequestBody RegisterPatientRequestDto requestDto) {
        userService.registerPatient(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/register-doctor")
    public ResponseEntity<String> registerDoctor(@Valid @RequestBody RegisterDoctorRequestDto requestDto) {
        userService.registerDoctor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        userService.verifyUserEmail(token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> verifyUserEmailAndSendResetPasswordEmail(@Valid @RequestBody ForgetPasswordRequestDto requestDto) {
        userService.sendResetPasswordEmail(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,
                                           @Valid @RequestBody ResetPasswordRequestDto requestDto) {
        userService.resetPassword(requestDto, token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginUserRequestDto requestDto) {
        AccessTokenDto responseDto = userService.loginUser(requestDto);
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
                .body(new UserIdResponseDto(responseDto.userId()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@CookieValue("Access-Token") String accessToken,
                                                   @CookieValue("Refresh-Token") String refreshToken) {
        @Valid RefreshTokenRequestDto requestDto = new RefreshTokenRequestDto(accessToken, refreshToken);
        AccessTokenDto responseDto = userService.refreshAccessToken(requestDto);
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
                .body(new UserIdResponseDto(responseDto.userId()));
    }

    @PostMapping("/logout")
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
