package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.appointments.messages.queries.GetConversationResponse;
import pl.edu.pja.aurorumclinic.features.appointments.messages.queries.MeGetAllConversations;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @NativeQuery(value = "exec AppointmentSlots :startedAt, :finishedAt, :pkService, :pkDoctor")
    List<Timestamp> appointmentSlots(LocalDateTime startedAt, LocalDateTime finishedAt, Long pkService, Long pkDoctor);

    @Query("""
        select d from Doctor d
        join d.specializations s
        join s.services serv
        where serv.id = :serviceId
          and (lower(d.name) like lower(concat('%', :query, '%'))
            or lower(d.surname) like lower(concat('%', :query, '%'))
            or lower(s.name) like lower(concat('%', :query, '%')))
        """)
    Page<Doctor> findAllByQueryAndServiceId(String query, Pageable pageable, Long serviceId);

    Page<Doctor> findBySpecializations_Services_Id(Long serviceId,
                                                 Pageable pageable);

    @NativeQuery("""
            select u1.pk_user, u1.name, u1.surname, d1.profile_picture from message join user_ u1 on u1.pk_user = message.fk_sender
            join doctor d1 on d1.pk_doctor = u1.pk_user
            join user_ u2 on u2.pk_user = message.fk_receiver where u2.pk_user = 2
            union
            select u2.pk_user, u2.name, u2.surname, d2.profile_picture from message join user_ u1 on u1.pk_user = message.fk_sender
            join user_ u2 on u2.pk_user = message.fk_receiver
            join doctor d2 on d2.pk_doctor = u2.pk_user where u1.pk_user = 2
            """)
    List<GetConversationResponse> findAllWhoHadConversationWithPatientId(Long patientId);
}
