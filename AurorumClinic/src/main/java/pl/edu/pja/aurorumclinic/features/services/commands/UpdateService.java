package pl.edu.pja.aurorumclinic.features.services.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UpdateService {

    private final ServiceRepository serviceRepository;

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> updateService(@PathVariable("id") Long serviceId,
                                                        @RequestBody @Valid UpdateServiceRequest request) {
        handle(serviceId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long serviceId, UpdateServiceRequest request) {
        Service serviceFromDb = serviceRepository.findById(serviceId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        serviceFromDb.setName(request.name());
        serviceFromDb.setDescription(request.description());
    }

    public record UpdateServiceRequest(@NotBlank @Size(max = 150) String name,
                                       @NotBlank @Size(max = 500) String description) {
    }

}
