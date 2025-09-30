package pl.edu.pja.aurorumclinic.features.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.GetPatientResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.PatchPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.PutPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.services.PatientService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('EMPLOYEE')")
public class PatientController {

    private final PatientService patientService;

    @GetMapping("")
    public ResponseEntity<?> getAllPatients(@RequestParam(required = false) String searchParam) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getAllPatients(searchParam)));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getPatient(@AuthenticationPrincipal Long id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable Long id,
                                           @Valid @RequestBody PutPatientRequest requestDto) {
        GetPatientResponse responseDto = patientService.updatePatient(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<?> partiallyUpdatePatient(@AuthenticationPrincipal Long id,
                                                    @Valid @RequestBody PatchPatientRequest requestDto) {
        GetPatientResponse responseDto = patientService.partiallyUpdatePatient(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<?> getPatientAppointments(@PathVariable("id") Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientAppointments(patientId)));
    }

}
