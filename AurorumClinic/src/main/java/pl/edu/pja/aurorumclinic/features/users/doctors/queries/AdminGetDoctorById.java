package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors/internal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminGetDoctorById {

    private final DoctorRepository doctorRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetDoctorByIdResponse>> adminGetDoctorById(@PathVariable("id") Long doctorId) {
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
                .pesel(doctorFromDb.getPesel())
                .birthdate(doctorFromDb.getBirthdate())
                .phoneNumber(doctorFromDb.getPhoneNumber())
                .email(doctorFromDb.getEmail())
                .twoFactorAuth(doctorFromDb.isTwoFactorAuth())
                .phoneNumberVerified(doctorFromDb.isPhoneNumberVerified())
                .pwzNumber(doctorFromDb.getPwzNumber())
                .specializationIds(doctorFromDb.getSpecializations().stream()
                        .map(Specialization::getId).collect(Collectors.toSet()))
                .build();
    }

    @Builder
    record GetDoctorByIdResponse(Long id,
                                 String name,
                                 String surname,
                                 String pesel,
                                 LocalDate birthdate,
                                 String phoneNumber,
                                 String email,
                                 boolean twoFactorAuth,
                                 boolean phoneNumberVerified,
                                 String pwzNumber,
                                 Set<Long> specializationIds) {

    }
}
