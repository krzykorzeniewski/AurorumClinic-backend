package pl.edu.pja.aurorumclinic.features.specializations.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;

@RestController
@RequestMapping("/api/specializations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CreateSpecialization {

    private final SpecializationRepository specializationRepository;

    @PostMapping("")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createSpecialization(
            @RequestBody @Valid CreateSpecializationRequest request) {
        handle(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(CreateSpecializationRequest request) {
        Specialization newSpecialization = Specialization.builder()
                .name(request.name)
                .build();
        specializationRepository.save(newSpecialization);
    }

    record CreateSpecializationRequest(@NotBlank @Size(max = 150) String name) {
    }

}
