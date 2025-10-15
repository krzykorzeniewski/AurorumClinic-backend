package pl.edu.pja.aurorumclinic.features.appointments.messages;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record MessageDto(@NotEmpty @Size(max = 500) String text,
                         @NotNull LocalDateTime sentAt,
                         @NotNull Long senderId) {
}
