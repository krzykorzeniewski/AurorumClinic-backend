package pl.edu.pja.aurorumclinic.features.users.doctors.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UpdateDoctor {

    private final DoctorRepository doctorRepository;
    private final SpecializationRepository specializationRepository;

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<UpdateDoctorResponse>> updateDoctor(@PathVariable("id") Long doctorId,
                                                                  @RequestBody @Valid UpdateDoctorRequest request) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, request)));
    }

    private UpdateDoctorResponse handle(Long doctorId, UpdateDoctorRequest request) {
        Doctor doctorFromDb = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        List<Specialization> specializationsFromDb = specializationRepository.findAllById(request.specializationIds);
        if (specializationsFromDb.size() > request.specializationIds().size()) {
            throw new ApiException("Some specialization ids are not found", "specializationIds");
        }
        doctorFromDb.setName(request.name);
        doctorFromDb.setSurname(request.surname);
        doctorFromDb.setPesel(request.pesel);
        doctorFromDb.setBirthdate(request.birthdate);
        doctorFromDb.setPhoneNumber(request.phoneNumber);
        doctorFromDb.setEmail(request.email);
        doctorFromDb.setTwoFactorAuth(request.twoFactorAuth);
        doctorFromDb.setPhoneNumberVerified(request.phoneNumberVerified);
        doctorFromDb.setEducation(request.education);
        doctorFromDb.setExperience(request.experience);
        doctorFromDb.setPwzNumber(request.pwzNumber);
        doctorFromDb.setSpecializations(new HashSet<>(specializationsFromDb));

        return UpdateDoctorResponse.builder()
                .id(doctorFromDb.getId())
                .name(doctorFromDb.getName())
                .surname(doctorFromDb.getSurname())
                .pesel(doctorFromDb.getPesel())
                .birthdate(doctorFromDb.getBirthdate())
                .phoneNumber(doctorFromDb.getPhoneNumber())
                .email(doctorFromDb.getEmail())
                .twoFactorAuth(doctorFromDb.isTwoFactorAuth())
                .phoneNumberVerified(doctorFromDb.isPhoneNumberVerified())
                .education(doctorFromDb.getEducation())
                .experience(doctorFromDb.getExperience())
                .pwzNumber(doctorFromDb.getPwzNumber())
                .specializations(doctorFromDb.getSpecializations().stream().map(specialization ->
                        UpdateDoctorResponse.SpecializationDto.builder()
                        .id(specialization.getId())
                        .name(specialization.getName())
                        .build()).collect(Collectors.toSet()))
                .build();

    }

    record UpdateDoctorRequest(@NotBlank @Size(max = 50) String name,
                               @NotBlank @Size(max = 50) String surname,
                               @NotBlank @Size(min = 11, max = 11) String pesel,
                               @NotNull LocalDate birthdate,
                               @NotBlank @Size(min = 9, max = 9) String phoneNumber,
                               @NotBlank @Email @Size(max = 100) String email,
                               boolean twoFactorAuth,
                               boolean phoneNumberVerified,
                               @NotBlank @Size(max = 100) String education,
                               @NotBlank @Size(max = 100) String experience,
                               @NotBlank @Size(max = 100) String pwzNumber,
                               @NotEmpty Set<Long> specializationIds) {

    }

    @Builder
    record UpdateDoctorResponse(Long id,
            String name,
            String surname,
            String pesel,
            LocalDate birthdate,
            String phoneNumber,
            String email,
            boolean twoFactorAuth,
            boolean phoneNumberVerified,
            String education,
            String experience,
            String pwzNumber,
            Set<SpecializationDto> specializations) {

        @Builder
        record SpecializationDto(Long id,
                                 String name){

        }
    }
}
