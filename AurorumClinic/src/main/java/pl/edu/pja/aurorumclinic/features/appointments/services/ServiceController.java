package pl.edu.pja.aurorumclinic.features.appointments.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping("")
    public ResponseEntity<?> createService(@RequestBody @Valid CreateServiceRequest createServiceRequest) {
        serviceService.createService(createServiceRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
