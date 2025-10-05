package pl.edu.pja.aurorumclinic.features.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.GetPatientAppointmentResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.GetPatientResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.PatchPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.PutPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.services.PatientService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<GetPatientResponse>>> getAllPatients(@RequestParam(required = false) String query,
                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getAllPatients(query, page, size)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<GetPatientResponse>> getPatient(@AuthenticationPrincipal Long id) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<GetPatientResponse>> updatePatient(@PathVariable Long id,
                                           @Valid @RequestBody PutPatientRequest requestDto) {
        GetPatientResponse responseDto = patientService.updatePatient(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<GetPatientResponse>> partiallyUpdatePatient(@AuthenticationPrincipal Long id,
                                                    @Valid @RequestBody PatchPatientRequest requestDto) {
        GetPatientResponse responseDto = patientService.partiallyUpdatePatient(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}/appointments")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Page<GetPatientAppointmentResponse>>> getPatientAppointments(@PathVariable("id") Long patientId,
                                                                                                   @RequestParam(defaultValue = "0") int page,
                                                                                                   @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientAppointments(patientId, page, size)));
    }

    @GetMapping("/me/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<Page<GetPatientAppointmentResponse>>> getMyAppointments(@AuthenticationPrincipal Long patientId,
                                                                                                   @RequestParam(defaultValue = "0") int page,
                                                                                                   @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientAppointments(patientId, page, size)));
    }

}
