package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.GetDoctorResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @NativeQuery(value = "exec AppointmentSlots :startedAt, :finishedAt, :pkService, :pkDoctor")
    List<Timestamp> appointmentSlots(LocalDateTime startedAt, LocalDateTime finishedAt, Long pkService, Long pkDoctor);

    @Query("""
           select new pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.GetDoctorResponse(
                    d.id, d.name, d.surname,
                               new pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.SpecializationDto(
                                          s.id, s.name),
                    d.profilePicture, cast((coalesce(avg(o.rating), 0)) as int)
                )
              from Doctor d
              left join d.appointments a
              left join a.opinion o
              left join d.specializations s
              where
              lower(d.name) like lower(concat('%', :query, '%')) or
              lower(d.surname) like lower(concat('%', :query, '%')) or
              lower(s.name) like lower(concat('%', :query, '%'))
              group by d.id, d.name, d.surname, d.profilePicture
           """)
    Page<GetDoctorResponse> findAllByQuery(String query, Pageable pageable);

    @Query("""
            select new pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.GetDoctorResponse(
                d.id, d.name, d.surname, new pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.SpecializationDto(
                                          s.id, s.name),
                d.profilePicture, cast(coalesce(avg(o.rating), 0) as int)
            )
                from Doctor d
                left join d.appointments a
                left join a.opinion o
                left join d.specializations s
            group by d.id, d.name, d.surname, d.profilePicture
            order by avg(o.rating) desc
""")
    Page<GetDoctorResponse> findAllByHighestRating(Pageable pageable);

    @Query("""
            select new pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.GetDoctorResponse(
                d.id, d.name, d.surname, new pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.SpecializationDto(
                                          s.id, s.name),
                d.profilePicture, cast(coalesce(avg(o.rating), 0) as int)
            )
                from Doctor d
                left join d.appointments a
                left join a.opinion o
                left join d.specializations s
            group by d.id, d.name, d.surname, d.profilePicture
            """)
    Page<GetDoctorResponse> findAllResponseDtos(Pageable pageable);
}
