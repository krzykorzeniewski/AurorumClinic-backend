package pl.edu.pja.aurorumclinic.features.appointments.payments;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Payment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentRepository paymentRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleAppointmentCreatedEvent(AppointmentCreatedEvent event) {
        Appointment appointment = event.getAppointment();
        BigDecimal appointmentPrice = appointment.getService().getPrice();
        Payment payment = Payment.builder()
                .amount(appointmentPrice)
                .createdAt(LocalDateTime.now())
                .status(PaymentStatus.CREATED)
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        appointment.setPayment(savedPayment);
    }

}
