package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import jakarta.annotation.security.PermitAll;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@PermitAll
public class GetDoctorById {

    private final DoctorRepository doctorRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetDoctorByIdResponse>> getDoctorById(@PathVariable("id") Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId)));
    }

    private GetDoctorByIdResponse handle(Long doctorId) {
        Doctor doctorFromDb = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        return GetDoctorByIdResponse.builder()
                .id(doctorFromDb.getId())
                .name(doctorFromDb.getName())
                .surname(doctorFromDb.getSurname())
                .specializations(doctorFromDb.getSpecializations().stream().map(specialization -> GetDoctorByIdResponse.SpecializationDto.builder()
                        .id(specialization.getId())
                        .name(specialization.getName())
                        .build()).toList())
                .profilePicture(objectStorageService.generateUrl(doctorFromDb.getProfilePicture()))
                .rating((int) doctorFromDb.getAppointments().stream()
                        .map(Appointment::getOpinion)
                        .filter(Objects::nonNull)
                        .mapToInt(Opinion::getRating)
                        .average()
                        .orElse(0.0))
                .email(doctorFromDb.getEmail())
                .education(doctorFromDb.getEducation())
                .experience(doctorFromDb.getExperience())
                .description(doctorFromDb.getDescription())
                .build();
    }

    @Builder
    record GetDoctorByIdResponse(Long id,
                                 String name,
                                 String surname,
                                 List<SpecializationDto> specializations,
                                 String profilePicture,
                                 int rating,
                                 String email,
                                 String education,
                                 String experience,
                                 String description) {

        @Builder
        record SpecializationDto(Long id,
                                 String name) {

        }
    }

}
