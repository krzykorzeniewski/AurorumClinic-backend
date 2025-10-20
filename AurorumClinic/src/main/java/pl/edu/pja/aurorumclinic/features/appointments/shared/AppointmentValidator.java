package pl.edu.pja.aurorumclinic.features.appointments.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AppointmentValidator {

    private final AppointmentRepository appointmentRepository;

    public void validateTimeSlot(LocalDateTime startedAt, LocalDateTime finishedAt, Doctor doctor, Service service) {
        validateSpecialization(doctor, service);
        if (!appointmentRepository.isTimeSlotAvailable(startedAt, finishedAt, doctor.getId(), service.getId())) {
            throw new ApiException("Timeslot is not available", "appointment");
        }
    }
    private void validateSpecialization(Doctor doctor, Service service) {
        for (Specialization specialization: doctor.getSpecializations()) {
            if (specialization.getServices().contains(service)) {
                return;
            }
        }
        throw new ApiException("Doctor specialization is not assigned to this service", "specialization");
    }

}
