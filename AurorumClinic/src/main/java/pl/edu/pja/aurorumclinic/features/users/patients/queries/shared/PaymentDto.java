package pl.edu.pja.aurorumclinic.features.users.patients.queries.shared;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentDto(Long id,
                         @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal amount,
                         @JsonFormat(shape = JsonFormat.Shape.STRING) PaymentStatus status) {
}
