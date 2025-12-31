package pl.edu.pja.aurorumclinic.features.services.commands;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UpdateService {

    private final ServiceRepository serviceRepository;
    private final SpecializationRepository specializationRepository;

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
        List<Specialization> specializationsFromDb = specializationRepository.findAllById(request.specializationIds);
        if (specializationsFromDb.size() != request.specializationIds().size()) {
            throw new ApiException("Some specialization ids are not found", "specializationIds");
        }
        serviceFromDb.setName(request.name());
        serviceFromDb.setDuration(request.duration());
        serviceFromDb.setDescription(request.description());
        serviceFromDb.setPrice(request.price());
        serviceFromDb.setSpecializations(new HashSet<>(specializationsFromDb));
    }

    public record UpdateServiceRequest(@NotBlank @Size(max = 150) String name,
                                       @NotNull int duration,
                                       @NotNull @Digits(integer = 10, fraction = 2) @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price,
                                       @NotBlank @Size(max = 500) String description,
                                       @NotEmpty Set<Long> specializationIds) {
    }


}
