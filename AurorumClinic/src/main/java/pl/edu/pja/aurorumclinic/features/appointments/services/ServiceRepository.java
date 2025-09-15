package pl.edu.pja.aurorumclinic.features.appointments.services;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}
