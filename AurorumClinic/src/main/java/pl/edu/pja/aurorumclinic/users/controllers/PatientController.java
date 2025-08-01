package pl.edu.pja.aurorumclinic.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.users.dtos.GetPatientResponseDto;
import pl.edu.pja.aurorumclinic.users.dtos.PatchPatientRequestDto;
import pl.edu.pja.aurorumclinic.users.services.PatientService;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("")
    public ResponseEntity<?> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetPatientResponseDto> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient() {
        return null;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partiallyUpdatePatient(@PathVariable Long id,
                                                    @Valid @RequestBody PatchPatientRequestDto requestDto) {
        GetPatientResponseDto responseDto = patientService.partiallyUpdatePatient(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }


}
