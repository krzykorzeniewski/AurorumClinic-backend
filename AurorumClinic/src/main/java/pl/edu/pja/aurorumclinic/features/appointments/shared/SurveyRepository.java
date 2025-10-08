package pl.edu.pja.aurorumclinic.features.appointments.shared;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Survey;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
}
