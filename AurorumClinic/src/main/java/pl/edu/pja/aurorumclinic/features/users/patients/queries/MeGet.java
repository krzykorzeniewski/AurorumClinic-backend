package pl.edu.pja.aurorumclinic.features.users.patients.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class MeGet {

    private final PatientRepository patientRepository;

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ApiResponse<GetPatientResponse>> getPatient(@AuthenticationPrincipal Long id) {
        return ResponseEntity.ok(ApiResponse.success(handle(id)));
    }

    private GetPatientResponse handle(Long id) {
        return patientRepository.getPatientById(id);
    }

}
