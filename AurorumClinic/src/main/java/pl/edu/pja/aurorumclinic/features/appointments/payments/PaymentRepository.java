package pl.edu.pja.aurorumclinic.features.appointments.payments;


import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
