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

    @PostMapping("/login")
    public ResponseEntity<AccessTokenDto> loginUser(@Valid @RequestBody LoginUserRequestDto requestDto) {
        AccessTokenDto responseDto = userService.loginUser(requestDto);
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", responseDto.accessToken())
                .path("/")
                .httpOnly(true)
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", responseDto.refreshToken())
                .path("/")
                .httpOnly(true)
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshAccessToken(@CookieValue("Access-Token") String accessToken,
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
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .build();
    }

}
