package pl.edu.pja.aurorumclinic.features.users.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ServiceDto(Long id,
                  String name,
                  @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal price) {
}
