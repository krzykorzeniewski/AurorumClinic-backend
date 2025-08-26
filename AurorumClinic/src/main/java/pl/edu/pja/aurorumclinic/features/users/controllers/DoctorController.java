package pl.edu.pja.aurorumclinic.features.users.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pja.aurorumclinic.features.users.services.DoctorService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

import java.io.IOException;


@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("")
    public ResponseEntity<?> getAllDoctors() throws IOException {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getAllDoctors()));
    }

    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("image") MultipartFile image,
                                                  @PathVariable Long id) throws IOException {
        doctorService.uploadProfilePicture(image, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
