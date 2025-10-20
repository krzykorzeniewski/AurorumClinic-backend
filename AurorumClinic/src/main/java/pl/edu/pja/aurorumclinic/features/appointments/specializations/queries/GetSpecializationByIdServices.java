package pl.edu.pja.aurorumclinic.features.appointments.specializations.queries;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.security.PermitAll;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/specializations")
@RequiredArgsConstructor
@PermitAll
public class GetSpecializationByIdServices {

    private final SpecializationRepository specializationRepository;
    private final ServiceRepository serviceRepository;

    @GetMapping("/{id}/services")
    public ResponseEntity<ApiResponse<Page<GetSpecByIdServicesResponse>>> getByIdServices(
            @PathVariable("id") Long specId, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(handle(specId, pageable)));
    }

    private Page<GetSpecByIdServicesResponse> handle(Long specId, Pageable pageable) {
        specializationRepository.findById(specId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Page<Service> services = serviceRepository.getAllServicesBySpecializationId(specId, pageable);
        return services.map(service -> GetSpecByIdServicesResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .price(service.getPrice())
                .build());
    }

    @Builder
    public record GetSpecByIdServicesResponse(Long id,
                                              String name,
                                              @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price) {

    }

}
