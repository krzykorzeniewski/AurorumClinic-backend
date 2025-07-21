package pl.edu.pja.aurorumclinic.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.pja.aurorumclinic.users.dtos.LoginPatientRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.RefreshTokenRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.RegisterPatientRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.TokenResponseDto;
import pl.edu.pja.aurorumclinic.users.services.PatientService;


@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping("/register")
    public ResponseEntity<String> registerPatient(@Valid @RequestBody RegisterPatientRequestDto requestDto) {
        try {
            patientService.registerPatient(requestDto);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> loginPatient(@Valid @RequestBody LoginPatientRequestDto requestDto) {
        TokenResponseDto responseDto = patientService.loginPatient(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshAccessToken(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
        TokenResponseDto responseDto = patientService.refreshAccessToken(requestDto);
        return ResponseEntity.ok(responseDto);
    }

}
