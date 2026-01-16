package pl.edu.pja.aurorumclinic.features.appointments.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinishAppointmentJob {

    private final AppointmentRepository appointmentRepository;

    @Transactional
    public void execute(Long appointmentId) {
        try {
            Appointment appointmentFromDb = appointmentRepository.findById(appointmentId).orElseThrow(
                    () -> new ApiNotFoundException("Id not found", "id")
            );
            appointmentFromDb.setStatus(AppointmentStatus.FINISHED);
        } catch (Exception e) {
            log.error("Error executing FinishAppointmentJob for appointmentId: {}", appointmentId, e);
            throw e;
        }
    }

}
