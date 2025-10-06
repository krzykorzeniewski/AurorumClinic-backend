package pl.edu.pja.aurorumclinic.features.appointments.payments;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Payment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentMethod;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasAnyRole('PATIENT', 'EMPLOYEE')")
public class MockUpdatePayment {

    private final PaymentRepository paymentRepository;

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> placePayment(@PathVariable("id") Long paymentId,
                                                       @RequestBody @Valid UpdatePaymentRequest paymentRequest) {
        handle(paymentId, paymentRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long paymentId, UpdatePaymentRequest paymentRequest) {
        Payment paymentFromDb = paymentRepository.findById(paymentId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!Objects.equals(paymentFromDb.getStatus(), PaymentStatus.CREATED)) {
            throw new ApiException("Payment is already settled", "status");
        }
        paymentFromDb.setMethod(paymentRequest.paymentMethod);
        paymentFromDb.setStatus(PaymentStatus.COMPLETED);
        paymentFromDb.setCompletedAt(LocalDateTime.now());
    }

    public record UpdatePaymentRequest(@JsonFormat(shape = JsonFormat.Shape.STRING) PaymentMethod paymentMethod
                                       ) {
    }

}
