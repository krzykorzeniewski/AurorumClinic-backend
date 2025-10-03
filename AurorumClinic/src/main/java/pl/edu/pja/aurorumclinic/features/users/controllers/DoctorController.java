package pl.edu.pja.aurorumclinic.features.users.controllers;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pja.aurorumclinic.features.users.services.DoctorService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

import java.io.IOException;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PermitAll
    @GetMapping("/search")
    public ResponseEntity<?> searchAllDoctors(@RequestParam String query,
                                              @RequestParam (defaultValue = "0") int page,
                                              @RequestParam (defaultValue = "5") int size) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(doctorService.searchAllDoctors(query, page, size)));
    }

    @PermitAll
    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedDoctors(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam (defaultValue = "5") int size) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getRecommendedDoctors(page, size)));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/me/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam MultipartFile image,
                                                  @AuthenticationPrincipal Long doctorId) throws IOException {
        doctorService.uploadProfilePicture(image, doctorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PermitAll
    @GetMapping("/{id}/appointment-slots")
    public ResponseEntity<?> getAppointmentSlots(@PathVariable Long id,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startedAt,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime finishedAt,
                          @RequestParam int serviceDuration) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getAppointmentSlots(id, startedAt,
                finishedAt, serviceDuration)));
    }

}
