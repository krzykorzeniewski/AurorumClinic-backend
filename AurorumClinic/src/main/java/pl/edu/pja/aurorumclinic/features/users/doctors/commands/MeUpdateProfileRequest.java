package pl.edu.pja.aurorumclinic.features.users.doctors.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MeUpdateProfileRequest(
        @NotBlank @Size(max = 100) String experience,
        @NotBlank @Size(max = 100) String education,
        @NotBlank @Size(max = 500) String description) {}
