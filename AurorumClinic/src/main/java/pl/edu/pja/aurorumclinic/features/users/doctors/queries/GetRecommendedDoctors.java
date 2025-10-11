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
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.util.Comparator;
import java.util.Objects;

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
        Page<Doctor> doctorsFromDb = doctorRepository.findAll(pageable);
        Page<GetDoctorResponse> response = doctorsFromDb.map(doctor -> GetDoctorResponse.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .surname(doctor.getSurname())
                .specializations(doctor.getSpecializations().stream().map(
                        specialization -> GetDoctorResponse.SpecializationDto.builder()
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
                .build());
        return response;
    }

}
