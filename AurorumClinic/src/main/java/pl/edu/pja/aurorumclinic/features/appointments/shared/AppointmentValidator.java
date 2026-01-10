package pl.edu.pja.aurorumclinic.features.appointments.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AppointmentValidator {

    private final AppointmentRepository appointmentRepository;

    public void validateAppointment(LocalDateTime startedAt, LocalDateTime finishedAt, Doctor doctor, Service service) {
        if (startedAt.isAfter(finishedAt)) {
            throw new ApiException("Start date cannot be after end date", "startedAt");
        }
        if (finishedAt.isBefore(startedAt)) {
            throw new ApiException("End date cannot be before start date", "finishedAt");
        }
        if (startedAt.isBefore(LocalDateTime.now())) {
            throw new ApiException("Start date cannot be in the past", "startedAt");
        }
        validateSpecialization(doctor, service);
        if (!appointmentRepository.isTimeSlotAvailable(startedAt, finishedAt, doctor.getId(), service.getId())) {
            throw new ApiException("Timeslot is not available", "appointment");
        }
    }

    public void validateRescheduledAppointment(LocalDateTime startedAt, LocalDateTime finishedAt, Doctor doctor,
                                               Service service, Appointment appointment) {
        if (startedAt.isAfter(finishedAt)) {
            throw new ApiException("Start date cannot be after end date", "startedAt");
        }
        if (finishedAt.isBefore(startedAt)) {
            throw new ApiException("End date cannot be before start date", "finishedAt");
        }
        if (startedAt.isBefore(LocalDateTime.now())) {
            throw new ApiException("Start date cannot be in the past", "startedAt");
        }
        validateSpecialization(doctor, service);
        if (!appointmentRepository.isTimeSlotAvailableExcludingId(startedAt, finishedAt, doctor.getId(), service.getId(),
                appointment.getId())) {
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
