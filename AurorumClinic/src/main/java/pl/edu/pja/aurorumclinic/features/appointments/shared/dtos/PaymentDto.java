package pl.edu.pja.aurorumclinic.features.appointments.shared.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;

import java.math.BigDecimal;

record PaymentDto(Long id,
                         @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) BigDecimal amount,
                         @JsonFormat(shape = JsonFormat.Shape.STRING)PaymentStatus status) {
}
