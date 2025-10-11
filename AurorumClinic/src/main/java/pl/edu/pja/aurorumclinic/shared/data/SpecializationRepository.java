package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
}
