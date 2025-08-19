package pl.edu.pja.aurorumclinic.features.users.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.users.services.DoctorService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;


@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("")
    public ResponseEntity<?> getAllDoctors() {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getAllDoctors()));
    }

}
