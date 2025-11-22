package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;

import java.util.List;

public interface OpinionRepository extends JpaRepository<Opinion, Long> {

    List<Opinion> findByAppointment_Doctor_IdOrderByCreatedAtDesc(Long doctorId);
}