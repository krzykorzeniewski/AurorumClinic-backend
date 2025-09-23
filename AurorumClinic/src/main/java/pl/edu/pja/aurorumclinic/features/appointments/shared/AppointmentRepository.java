package pl.edu.pja.aurorumclinic.features.appointments.shared;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.response.GetAppointmentPatientResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;

import java.time.LocalDateTime;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
    select case
             when exists (
                 select 1
                 from Schedule s
                 where s.doctor.id = :doctorId
                   and s.startedAt <= :startedAt
                   and s.finishedAt >= :finishedAt
             )
             and not exists (
                 select 1
                 from Appointment a
                 where a.doctor.id = :doctorId
                   and a.startedAt <= :finishedAt
                   and a.finishedAt >= :startedAt
             )
             then true
             else false
           end
    """)
    boolean timeSlotExists(LocalDateTime startedAt, LocalDateTime finishedAt,
                           Long doctorId, Long serviceId);

    @Query("""
            select new
            pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.response.GetAppointmentPatientResponse(
                        d.name, d.surname, d.profilePicture, s.name, s.price, a.startedAt)
                        from Appointment a
                        join Doctor d on d.id = a.doctor.id
                        join Service s on s.id = a.service.id
                        join Patient p on p.id = a.patient.id
            where a.id = :appointmentId and p.id = :patientId
            """)
    GetAppointmentPatientResponse findByIdAndPatientId(Long appointmentId, Long patientId);
}
