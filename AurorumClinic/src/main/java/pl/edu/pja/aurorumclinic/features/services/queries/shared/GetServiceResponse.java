package pl.edu.pja.aurorumclinic.features.services.queries.shared;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public record GetServiceResponse(Long id,
                                 String name,
                                 @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price,
                                 int duration,
                                 String description) {
}
