package pl.edu.pja.aurorumclinic.features.appointments.services;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateServiceRequest(@NotBlank @Size(max = 150) String name,
                                   @NotNull Integer duration,
                                   @NotNull @Digits(integer = 10, fraction = 2)
                                   @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price,
                                   @NotBlank @Size(max = 500) String description) {
}
