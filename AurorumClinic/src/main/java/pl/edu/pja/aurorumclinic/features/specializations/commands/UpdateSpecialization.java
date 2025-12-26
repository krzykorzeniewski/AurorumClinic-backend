package pl.edu.pja.aurorumclinic.features.specializations.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/specializations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UpdateSpecialization {

    private final SpecializationRepository specializationRepository;

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> updateSpecialization(@PathVariable("id") Long specializationId,
            @RequestBody @Valid UpdateSpecializationRequest request) {
        handle(specializationId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long specializationId, UpdateSpecializationRequest request) {
        Specialization specializationFromDb = specializationRepository.findById(specializationId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "specializationId")
        );
        specializationFromDb.setName(request.name);
    }

    public record UpdateSpecializationRequest(@NotBlank @Size(max = 150) String name){

    }

}
