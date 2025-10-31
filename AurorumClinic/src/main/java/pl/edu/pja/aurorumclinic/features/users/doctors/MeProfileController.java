package pl.edu.pja.aurorumclinic.features.users.doctors;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.users.doctors.commands.MeUpdateProfile;
import pl.edu.pja.aurorumclinic.features.users.doctors.commands.MeUpdateProfileRequest;
import pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.DoctorProfileResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/users/doctors/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class MeProfileController {

    private final DoctorRepository doctorRepository;
    private final MeUpdateProfile meUpdateProfile;
    private final DoctorProfileMapper mapper = new DoctorProfileMapper();

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Long userID
    ) {

        Doctor doctor = doctorRepository.findById(userID)
                .orElseThrow(() -> new ApiNotFoundException("Doktor nie zostal znaleziony", "email"));
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(doctor)));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal Long userID,
            @RequestBody @Valid MeUpdateProfileRequest request
    ) {
        Doctor updated = meUpdateProfile.handle(userID, request);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(updated)));
    }
}
