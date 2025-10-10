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
import pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.GetDoctorResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class GetRecommendedDoctors {

    private final DoctorRepository doctorRepository;
    private final ObjectStorageService objectStorageService;

    @PermitAll
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<Page<GetDoctorResponse>>> getRecommendedDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam (defaultValue = "5") int size)  {
        return ResponseEntity.ok(ApiResponse.success(handle(page, size)));
    }

    private Page<GetDoctorResponse> handle (int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GetDoctorResponse> doctorsFromDb = doctorRepository.findAllByHighestRating(pageable);
        doctorsFromDb.forEach(r -> {
            r.setProfilePicture(objectStorageService.
                    generateUrl(r.getProfilePicture()));
        });
        return doctorsFromDb;
    }

}
