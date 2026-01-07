package pl.edu.pja.aurorumclinic.features.specializations.queries;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.specializations.queries.shared.GetSpecializationResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;


@RestController
@RequestMapping("/api/specializations")
@RequiredArgsConstructor
@PermitAll
public class GetAllSpecializations {

    private final SpecializationRepository specializationRepository;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<GetSpecializationResponse>>> getAll(
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(handle(pageable)));
    }

    private Page<GetSpecializationResponse> handle(Pageable pageable) {
        return specializationRepository.findAllSpecializationDtos(pageable);
    }

}
