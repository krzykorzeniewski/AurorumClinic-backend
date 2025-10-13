package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import jakarta.annotation.security.PermitAll;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class GetRecommendedDoctors {

    private final DoctorRepository doctorRepository;
    private final ObjectStorageService objectStorageService;

    @PermitAll
    @GetMapping("/recommended")

    public ResponseEntity<ApiResponse<List<GetRecommendedDoctorResponse>>> getRecommendedDoctors(
                        @PageableDefault(size = 6) Pageable pageable)  {
        return ResponseEntity.ok(ApiResponse.success(handle(pageable)));
    }

    private List<GetRecommendedDoctorResponse> handle (Pageable pageable) {
        Page<Doctor> doctorsFromDb = doctorRepository.findAll(pageable);
        List<GetRecommendedDoctorResponse> response = doctorsFromDb.map(doctor -> GetRecommendedDoctorResponse.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .surname(doctor.getSurname())
                .specializations(doctor.getSpecializations().stream().map(
                        specialization -> GetRecommendedDoctorResponse.SpecializationDto.builder()
                                .id(specialization.getId())
                                .name(specialization.getName())
                                .build()
                ).toList())
                .profilePicture(objectStorageService.generateUrl(doctor.getProfilePicture()))
                .rating((int) doctor.getAppointments().stream()
                        .map(Appointment::getOpinion)
                        .filter(Objects::nonNull)
                        .mapToInt(Opinion::getRating)
                        .average()
                        .orElse(0.0))
                .build()).stream().sorted(Comparator.comparing(GetRecommendedDoctorResponse::rating).reversed()).toList();
        return response;
    }

    @Builder
    record GetRecommendedDoctorResponse(
            Long id,
            String name,
            String surname,
            List<SpecializationDto> specializations,
            String profilePicture,
            int rating) {
        @Builder
        record SpecializationDto(Long id,
                                 String name) {
        }

    }

}
