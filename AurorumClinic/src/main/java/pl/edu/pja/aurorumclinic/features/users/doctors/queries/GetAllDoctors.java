package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import java.util.Objects;


@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class GetAllDoctors {

    private final DoctorRepository doctorRepository;
    private final ObjectStorageService objectStorageService;

    @PermitAll
    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<GetDoctorResponse>>> searchAllDoctors(
                                    @RequestParam(required = false) String query,
                                    @RequestParam(required = true) Long serviceId,
                                    @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(handle(query, pageable, serviceId)));
    }

    private Page<GetDoctorResponse> handle(String query, Pageable pageable, Long serviceId) {
        Page<Doctor> doctorsFromDb;
        if (query == null) {
            doctorsFromDb = doctorRepository.findAllByServiceId(pageable, serviceId);
        } else {
            doctorsFromDb =  doctorRepository.findAllByQueryAndServiceId(query, pageable, serviceId);
        }
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
