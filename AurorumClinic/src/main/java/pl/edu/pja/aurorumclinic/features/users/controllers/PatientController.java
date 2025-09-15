package pl.edu.pja.aurorumclinic.features.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.users.dtos.GetPatientResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.PatchPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.PutPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.services.PatientService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("")
    public ResponseEntity<?> getAllPatients() {
        return ResponseEntity.ok(ApiResponse.success(patientService.getAllPatients()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable Long id,
                                           @Valid @RequestBody PutPatientRequest requestDto) {
        GetPatientResponse responseDto = patientService.updatePatient(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partiallyUpdatePatient(@PathVariable Long id,
                                                    @Valid @RequestBody PatchPatientRequest requestDto) {
        GetPatientResponse responseDto = patientService.partiallyUpdatePatient(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> partiallyUpdatePatient(@PathVariable Long id, Authentication authentication) {
        patientService.deletePatient(id, authentication);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
