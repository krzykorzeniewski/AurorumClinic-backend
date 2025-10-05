package pl.edu.pja.aurorumclinic.features.appointments.services;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.appointments.services.dtos.request.CreateServiceRequest;
import pl.edu.pja.aurorumclinic.features.appointments.services.dtos.request.UpdateServiceRequest;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping("")
    public ResponseEntity<?> createService(@RequestBody @Valid CreateServiceRequest createServiceRequest) {
        serviceService.createService(createServiceRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PermitAll
    @GetMapping("")
    public ResponseEntity<?> getAllServices(@RequestParam (defaultValue = "0") int page,
                                            @RequestParam (defaultValue = "5") int size) {
        return ResponseEntity.ok(ApiResponse.success(serviceService.getAll(page, size)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateService(@PathVariable("id") Long serviceId,
                                                        @RequestBody @Valid UpdateServiceRequest request) {
        serviceService.updateService(serviceId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteService(@PathVariable("id") Long serviceId) {
        serviceService.deleteService(serviceId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }


}
