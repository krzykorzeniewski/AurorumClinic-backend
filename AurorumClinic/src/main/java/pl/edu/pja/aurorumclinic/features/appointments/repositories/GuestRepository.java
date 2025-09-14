package pl.edu.pja.aurorumclinic.features.appointments.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Guest;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    Guest findByAppointmentDeleteToken(String appointmentDeleteToken);
}
