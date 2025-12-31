package pl.edu.pja.aurorumclinic.features.services.commands;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CreateService {

    private final ServiceRepository serviceRepository;
    private final SpecializationRepository specializationRepository;

    @PostMapping("")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createService(@RequestBody @Valid CreateServiceRequest createServiceRequest) {
        handle(createServiceRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(CreateServiceRequest request) {
        List<Specialization> specializationsFromDb = specializationRepository.findAllById(request.specializationIds);
        if (specializationsFromDb.size() != request.specializationIds().size()) {
            throw new ApiException("Some specialization ids are not found", "specializationIds");
        }
        Service service = Service.builder()
                .name(request.name())
                .price(request.price())
                .duration(request.duration())
                .description(request.description())
                .specializations(new HashSet<>(specializationsFromDb))
                .build();
        serviceRepository.save(service);
    }

    public record CreateServiceRequest(@NotBlank @Size(max = 150) String name,
                                       @NotNull Integer duration,
                                       @NotNull @Digits(integer = 10, fraction = 2) @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price,
                                       @NotBlank @Size(max = 500) String description,
                                       @NotEmpty Set<Long> specializationIds) {
    }


}
