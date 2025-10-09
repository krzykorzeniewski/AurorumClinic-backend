package pl.edu.pja.aurorumclinic.features.appointments.shared.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public record ServiceDto(Long id,
                         String name,
                         @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price) {
}
