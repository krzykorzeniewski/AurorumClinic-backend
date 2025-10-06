package pl.edu.pja.aurorumclinic.features.appointments.payments;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
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

    @Async
    @TransactionalEventListener
    public void handleAppointmentCreatedEvent(AppointmentCreatedEvent event) {
        Appointment appointment = event.getAppointment();
        BigDecimal appointmentPrice = appointment.getService().getPrice();
        Payment payment = Payment.builder()
                .amount(appointmentPrice)
                .createdAt(LocalDateTime.now())
                .status(PaymentStatus.CREATED)
                .appointment(appointment)
                .build();
        paymentRepository.save(payment);
    }

}
