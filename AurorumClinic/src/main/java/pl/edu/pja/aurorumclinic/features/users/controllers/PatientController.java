package pl.edu.pja.aurorumclinic.features.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.users.dtos.GetPatientResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.PatchPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.PutPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.services.PatientService;

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
    public ResponseEntity<GetPatientResponse> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable Long id,
                                           @Valid @RequestBody PutPatientRequest requestDto) {
        GetPatientResponse responseDto = patientService.updatePatient(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partiallyUpdatePatient(@PathVariable Long id,
                                                    @Valid @RequestBody PatchPatientRequest requestDto) {
        GetPatientResponse responseDto = patientService.partiallyUpdatePatient(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }


}
