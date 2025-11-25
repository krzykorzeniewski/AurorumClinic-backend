package pl.edu.pja.aurorumclinic.features.specializations.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.specializations.queries.shared.GetSpecializationResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/specializations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GetSpecializationById {

    private final SpecializationRepository specializationRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetSpecializationResponse>> getSpecializationById(@PathVariable("id") Long specId) {
        return ResponseEntity.ok(ApiResponse.success(handle(specId)));
    }

    private GetSpecializationResponse handle(Long specializationId) {
        return specializationRepository.findSpecializationDtoById(specializationId).orElseThrow(() ->
                new ApiNotFoundException("Id not found", "specializationId"));
    }

}
