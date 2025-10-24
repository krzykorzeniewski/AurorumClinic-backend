package pl.edu.pja.aurorumclinic.features.users.doctors.commands;

import jakarta.validation.constraints.Size;

public record MeUpdateProfileRequest(
        @Size(max = 100) String experience,
        @Size(max = 100) String education,
        @Size(max = 500) String description,
        @Size(max = 7) String pwzNumber
) {}
