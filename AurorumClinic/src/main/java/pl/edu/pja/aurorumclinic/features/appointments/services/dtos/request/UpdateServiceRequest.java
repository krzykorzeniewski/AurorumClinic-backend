package pl.edu.pja.aurorumclinic.features.appointments.services.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateServiceRequest(@NotBlank @Size(max = 150) String name,
                                   @NotNull int duration,
                                   @NotNull @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)  BigDecimal price,
                                   @NotBlank @Size(max = 500) String description) {
}
