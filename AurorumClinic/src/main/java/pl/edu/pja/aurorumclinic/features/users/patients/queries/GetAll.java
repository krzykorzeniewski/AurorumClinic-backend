package pl.edu.pja.aurorumclinic.features.users.patients.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class GetAll {

    private final PatientRepository patientRepository;

    @GetMapping("")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Page<GetPatientResponse>>> getAllPatients(@RequestParam(required = false) String query,
                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(ApiResponse.success(handle(query, page, size)));
    }

    private Page<GetPatientResponse> handle(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GetPatientResponse> patientsFromDb;
        if (query == null) {
            patientsFromDb = patientRepository.findAllGetPatientDtos(pageable);
        } else {
            patientsFromDb = patientRepository.searchAllByQuery(query, pageable);
        }
        return patientsFromDb;
    }

}
