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
@RequestMapping("/api/users/doctors/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DocUpdateProfile {

    private final DoctorRepository doctorRepository;
    private final DoctorProfileMapper mapper;
    private final ObjectStorageService objectStorageService;

    @PutMapping("/profile")
    @Transactional(rollbackFor = IOException.class)
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal Long userID,
            @RequestPart(value = "doctorImage", required = false) MultipartFile image,
            @RequestPart("command") @Valid MeUpdateProfileRequest command
    ) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(handle(userID, image, command)));
    }

    private DoctorProfileResponse handle(Long userID, MultipartFile image, MeUpdateProfileRequest command) throws IOException {
        Doctor doctor = doctorRepository.findById(userID)
                .orElseThrow(() -> new ApiNotFoundException("ID not found", "Id"));

        doctor.setExperience(command.experience());
        doctor.setEducation(command.education());
        doctor.setDescription(command.description());

        String imagePath = null;
        if (image != null) {
            imagePath = objectStorageService.uploadObject(image);
        }
        doctor.setProfilePicture(imagePath);

        return mapper.toResponse(doctor);
    }
}
