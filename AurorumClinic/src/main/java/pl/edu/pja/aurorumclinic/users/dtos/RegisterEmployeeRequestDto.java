package pl.edu.pja.aurorumclinic.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterEmployeeRequestDto(@NotBlank @Size(max = 50) String name,
                                         @NotBlank @Size(max = 50) String surname,
                                         @Size(min = 11, max = 11) String pesel,
                                         @NotNull LocalDate birthDate,
                                         @NotBlank @Email @Size(max = 100) String email,
                                         @NotBlank @Size(max = 200) String password,
                                         @NotBlank @Size(max = 9) String phoneNumber,
                                         @NotBlank @Size(max = 50) String role) {
}
