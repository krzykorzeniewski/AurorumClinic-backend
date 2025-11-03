package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.doctors.DoctorProfileMapper;
import pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.DoctorProfileResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/users/doctors/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DocGetDoctorByIdProfile {

    private final DoctorRepository doctorRepository;
    private final DoctorProfileMapper mapper;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Long userID
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(userID)));
    }

    private DoctorProfileResponse handle(Long userID) {
        Doctor doctor = doctorRepository.findById(userID)
                .orElseThrow(() -> new ApiNotFoundException("ID not found", "Id"));
        return mapper.toResponse(doctor);
    }

}