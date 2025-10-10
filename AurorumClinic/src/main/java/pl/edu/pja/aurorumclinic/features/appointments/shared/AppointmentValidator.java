package pl.edu.pja.aurorumclinic.features.appointments.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.features.appointments.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AppointmentValidator {

    private final AppointmentRepository appointmentRepository;

    public void validateTimeSlot(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId, Long serviceId) {
        if (!appointmentRepository.timeSlotExists(startedAt, finishedAt, doctorId, serviceId)) {
            throw new ApiException("Timeslot is not available", "appointment");
        }
    }


}
