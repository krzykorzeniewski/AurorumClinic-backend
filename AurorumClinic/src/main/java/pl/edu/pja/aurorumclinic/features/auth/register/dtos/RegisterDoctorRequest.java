package pl.edu.pja.aurorumclinic.features.auth.register.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;

public record RegisterDoctorRequest(@NotBlank @Size(max = 50) String name,
                                    @NotBlank @Size(max = 50) String surname,
                                    @Size(min = 11, max = 11) String pesel,
                                    @NotNull LocalDate birthDate,
                                    @NotBlank @Email @Size(max = 100) String email,
                                    @NotBlank @Size(max = 9) String phoneNumber,
                                    @NotBlank @Size(max = 100) String description,
                                    @NotBlank @Size(max = 100) String specialization,
                                    @NotBlank @Size(max = 100) String education,
                                    @NotBlank @Size(max = 100) String experience,
                                    @NotBlank @Size(min = 7, max = 7) String pwzNumber,
                                    @NotEmpty Set<Long> specializationIds) {
}
