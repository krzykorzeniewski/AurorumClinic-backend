package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.SearchDoctorResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.io.IOException;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class SearchAll {

    private final DoctorRepository doctorRepository;
    private final ObjectStorageService objectStorageService;

    @PermitAll
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<SearchDoctorResponse>>> searchAllDoctors(@RequestParam String query,
                                              @RequestParam (defaultValue = "0") int page,
                                              @RequestParam (defaultValue = "5") int size) throws IOException {
        return ResponseEntity.ok(ApiResponse.success(handle(query, page, size)));
    }

    private Page<SearchDoctorResponse> handle(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SearchDoctorResponse> result =  doctorRepository.findAllByQuery(query, pageable);
        result.forEach(r -> {
            if (r.getProfilePicture() != null) {
                    r.setProfilePicture(objectStorageService.
                            generateSignedUrl(r.getProfilePicture()));
            }
        });
        return result;
    }

}
