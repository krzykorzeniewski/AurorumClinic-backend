package pl.edu.pja.aurorumclinic.features.appointments.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}
