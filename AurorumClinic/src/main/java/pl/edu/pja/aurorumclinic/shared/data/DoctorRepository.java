package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.RecommendedDoctorResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.SearchDoctorResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @NativeQuery(value = "exec AppointmentSlots :startedAt, :finishedAt, :serviceDuration, :pkDoctor")
    List<Timestamp> appointmentSlots(LocalDateTime startedAt, LocalDateTime finishedAt,
                                     Integer serviceDuration, Integer pkDoctor);

    @Query("""
           select new pl.edu.pja.aurorumclinic.features.users.dtos.response.SearchDoctorResponse(
                    d.id, d.name, d.surname, d.specialization, d.profilePicture,
                                cast((coalesce(avg(o.rating), 0)) as int)
                )
                from Doctor d
                left join d.appointments a
                left join a.opinion o
                           where
              lower(d.name) like lower(concat('%', :query, '%')) or
              lower(d.surname) like lower(concat('%', :query, '%')) or
              lower(d.specialization) like lower(concat('%', :query, '%'))
              group by d.id, d.name, d.surname, d.specialization, d.profilePicture
           """)
    Page<SearchDoctorResponse> findAllByQuery(String query, Pageable pageable);

    @Query("""
            select new pl.edu.pja.aurorumclinic.features.users.dtos.response.RecommendedDoctorResponse(
                    d.id, d.name, d.surname, d.specialization, d.profilePicture,
                                cast((coalesce(avg(o.rating), 0)) as int)
                )
                from Doctor d
                left join d.appointments a
                left join a.opinion o
                group by d.id, d.name, d.surname, d.specialization, d.profilePicture
            """)
    Page<RecommendedDoctorResponse> findAllRecommendedDtos(Pageable pageable);
}
