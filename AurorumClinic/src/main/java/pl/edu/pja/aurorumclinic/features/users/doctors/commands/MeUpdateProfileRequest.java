package pl.edu.pja.aurorumclinic.features.users.doctors.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MeUpdateProfileRequest(
        @NotBlank @Size(max = 1000) String experience,
        @NotBlank @Size(max = 1000) String education,
        @NotBlank @Size(max = 5000) String description) {}
