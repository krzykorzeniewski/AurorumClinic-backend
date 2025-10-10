package pl.edu.pja.aurorumclinic.features.users.patients.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientAppointmentResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class GetPatientByIdAppointments {

    private final PatientRepository patientRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("/{id}/appointments")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Page<GetPatientAppointmentResponse>>> getPatientAppointments(
                                                                             @PathVariable("id") Long patientId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(ApiResponse.success(handle(patientId, page, size)));
    }

    private Page<GetPatientAppointmentResponse> handle(Long patientId, int page, int size) {
        if (!patientRepository.existsById(patientId)) {
            throw new ApiNotFoundException("Id not found", "id");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<GetPatientAppointmentResponse> patientAppointmentsById = patientRepository
                .findPatientAppointmentsById(patientId, pageable);
        patientAppointmentsById.forEach(a -> {
            a.doctor().setProfilePicture(objectStorageService.generateUrl(a.doctor().getProfilePicture()));
        });
        return patientAppointmentsById;
    }
}
