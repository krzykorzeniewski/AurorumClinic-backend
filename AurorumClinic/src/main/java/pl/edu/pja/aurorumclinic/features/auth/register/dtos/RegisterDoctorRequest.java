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
                                    @NotBlank @Size(max = 5000) String description,
                                    @NotBlank @Size(max = 1000) String education,
                                    @NotBlank @Size(max = 1000) String experience,
                                    @Size(max = 50) String pwzNumber,
                                    @NotEmpty Set<Long> specializationIds) {
}
