package pl.edu.pja.aurorumclinic.features.appointments.services.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public record GetServiceResponse(String name,
                                 @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price,
                                 int duration,
                                 String description) {
}
