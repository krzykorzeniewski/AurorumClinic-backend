package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;

public interface OpinionRepository extends JpaRepository<Opinion, Long> {

    @Query("select avg(o.rating) from Opinion o where o.appointment.doctor.id = :doctorId")
    Double getAverageRatingByDoctorId(@Param("doctorId") Long doctorId);

    long countByAppointment_Doctor_Id(Long doctorId);

    Page<Opinion> findByAppointment_Doctor_Id(Long doctorId, Pageable pageable);
}
