package pl.edu.pja.aurorumclinic.users.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.users.services.PatientService;
import pl.edu.pja.aurorumclinic.users.dtos.LoginPatientRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.RefreshTokenRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.TokenResponseDto;
import pl.edu.pja.aurorumclinic.users.dtos.RegisterPatientRequestDto;


@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerPatient(@RequestBody RegisterPatientRequestDto requestDto) {
        patientService.registerPatient(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> loginPatient(@RequestBody LoginPatientRequestDto requestDto) {
        TokenResponseDto responseDto = patientService.loginPatient(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshAccessToken(@RequestBody RefreshTokenRequestDto requestDto) throws Exception {
        TokenResponseDto responseDto = patientService.refreshAccessToken(requestDto);
        return ResponseEntity.ok(responseDto);
    }

}
