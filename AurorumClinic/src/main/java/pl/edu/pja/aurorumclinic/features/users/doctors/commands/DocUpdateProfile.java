package pl.edu.pja.aurorumclinic.features.users.doctors.commands;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pja.aurorumclinic.features.users.doctors.DoctorProfileMapper;
import pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.DoctorProfileResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DocUpdateProfile {

    private final DoctorRepository doctorRepository;
    private final DoctorProfileMapper mapper;
    private final ObjectStorageService objectStorageService;

    @Transactional
    @PatchMapping("/users/doctors/me/profile")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal Long userID,
            @RequestBody @Valid MeUpdateProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(handleUpdateProfile(userID, request)));
    }

    private DoctorProfileResponse handleUpdateProfile(Long userID, MeUpdateProfileRequest req) {
        Doctor d = doctorRepository.findById(userID)
                .orElseThrow(() -> new ApiNotFoundException("ID not found", "Id"));

        if (req.experience() != null)
            d.setExperience(trimOrNull(req.experience()));
        if (req.education() != null)
            d.setEducation(trimOrNull(req.education()));
        if (req.description() != null)
            d.setDescription(trimOrNull(req.description()));
        if (req.pwzNumber() != null)
            d.setPwzNumber(trimOrNull(req.pwzNumber()));

        return mapper.toResponse(d);
    }
    @PostMapping("/doctors/me/profile-picture")
    @Transactional(rollbackFor = {IOException.class})
    public ResponseEntity<ApiResponse<?>> uploadProfilePicture(
            @RequestParam MultipartFile image,
            @AuthenticationPrincipal Long doctorId
    ) throws IOException {
        handleUploadProfilePicture(image, doctorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handleUploadProfilePicture(MultipartFile image, Long doctorId) throws IOException {
        Doctor doctorFromDb = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        String imagePath = objectStorageService.uploadObject(image);
        doctorFromDb.setProfilePicture(imagePath);
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
