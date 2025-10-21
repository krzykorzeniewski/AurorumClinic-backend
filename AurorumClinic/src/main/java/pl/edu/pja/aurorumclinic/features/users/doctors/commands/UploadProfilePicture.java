package pl.edu.pja.aurorumclinic.features.users.doctors.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.io.IOException;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class UploadProfilePicture {

    private final DoctorRepository doctorRepository;
    private final ObjectStorageService objectStorageService;

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/me/profile-picture")
    @Transactional(rollbackFor = {IOException.class})
    public ResponseEntity<ApiResponse<?>> uploadProfilePicture(@RequestParam MultipartFile image,
                                                  @AuthenticationPrincipal Long doctorId) throws IOException {
        handle(image, doctorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(MultipartFile image, Long doctorId) throws IOException {
        Doctor doctorFromDb = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        String imagePath = objectStorageService.uploadObject(image);
        doctorFromDb.setProfilePicture(imagePath);
    }

}
