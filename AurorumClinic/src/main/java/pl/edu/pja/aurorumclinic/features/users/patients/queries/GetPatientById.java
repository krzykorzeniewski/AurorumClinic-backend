package pl.edu.pja.aurorumclinic.features.users.patients.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EMPLOYEE', 'DOCTOR')")
public class GetPatientById {

    private final PatientRepository patientRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetPatientResponse>> getPatientById(@PathVariable("id") Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(handle(patientId)));
    }

    private GetPatientResponse handle(Long patientId) {
        GetPatientResponse response = patientRepository.getPatientResponseDtoById(patientId);
        if (response == null) {
            throw new ApiNotFoundException("Id not found", "id");
        }
        return response;
    }

}
