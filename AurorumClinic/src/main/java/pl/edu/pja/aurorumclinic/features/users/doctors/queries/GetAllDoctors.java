package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class GetAllDoctors {

    private final DoctorRepository doctorRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<GetDoctorResponse>>> getAllDoctors(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(handle(pageable)));
    }

    private Page<GetDoctorResponse> handle(Pageable pageable) {
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

    @Builder
    record GetDoctorResponse(
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
