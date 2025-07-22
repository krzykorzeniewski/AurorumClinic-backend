package pl.edu.pja.aurorumclinic.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<AccessTokenResponseDto> loginUser(@Valid @RequestBody LoginUserRequestDto requestDto) {
        AccessTokenResponseDto responseDto = userService.loginUser(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponseDto> refreshAccessToken(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
        AccessTokenResponseDto responseDto = userService.refreshAccessToken(requestDto);
        return ResponseEntity.ok(responseDto);
    }

}
