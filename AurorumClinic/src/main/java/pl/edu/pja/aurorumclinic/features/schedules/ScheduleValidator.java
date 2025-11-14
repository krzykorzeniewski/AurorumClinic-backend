package pl.edu.pja.aurorumclinic.features.schedules;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ScheduleValidator {

    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AbsenceRepository absenceRepository;

    @Value("${workday.start.hour}")
    private Integer startOfDay;

    @Value("${workday.end.hour}")
    private Integer endOfDay;

    public void validateTimeslotAndServices(
            LocalDateTime startedAt, LocalDateTime finishedAt, Doctor doctor, List<Service> services) {
        validateTimeSlot(startedAt, finishedAt, doctor.getId());
        validateSpecializations(doctor, services);
    }

    public void validateNewTimeslotAndServices(
            LocalDateTime startedAt, LocalDateTime finishedAt, Doctor doctor, List<Service> services, Schedule schedule) {
        validateNewTimeSlot(startedAt, finishedAt, doctor.getId(), schedule);
        validateSpecializations(doctor, services);
    }

    public void checkIfScheduleHasAppointments(Schedule schedule) {
        if (appointmentRepository.existsByService_Schedules_Id(schedule.getId())) {
            throw new ApiException("Schedule has appointments assigned", "appointments");
        }
    }

    public void checkIfScheduleHasAppointmentsInOldTimeslot(Schedule schedule,
                                                            LocalDateTime newStartedAt, LocalDateTime newFinishedAt) {
        LocalDateTime oldStartedAt = schedule.getStartedAt();
        LocalDateTime oldFinishedAt = schedule.getFinishedAt();
        if (newStartedAt.isBefore(oldStartedAt) && newFinishedAt.isAfter(oldFinishedAt)) {
            return;
        }
        Set<Long> appointmentIdsInTimeslot = appointmentRepository.getAppointmentsInScheduleTimeslot
                (schedule.getId(), oldStartedAt, oldFinishedAt, newStartedAt, newFinishedAt);

        if (!appointmentIdsInTimeslot.isEmpty()) {
            throw new ApiException(appointmentIdsInTimeslot.toString(), "appointments");
        }
    }

    private void validateSpecializations(Doctor doctor, List<Service> services) {
        for (Service service: services) {
            for (Specialization specialization: doctor.getSpecializations()) {
                if(specialization.getServices().contains(service)) {
                    return;
                }
            }
        }
        throw new ApiException("Doctor specialization is not assigned to this service", "specialization");
    }

    private void validateTimeSlot(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId) {
        if (!Objects.equals(startedAt.getDayOfMonth(), finishedAt.getDayOfMonth())) {
            throw new ApiException("Schedule can be one day long", "startedAt, finishedAt");
        }
        if (startedAt.getHour() < startOfDay) {
            throw new ApiException("Start date cannot be before work hours", "startedAt");
        }
        if (finishedAt.getHour() > endOfDay) {
            throw new ApiException("End date cannot be after work hours", "finishedAt");
        }
        if (startedAt.isAfter(finishedAt)) {
            throw new ApiException("Start date cannot be after end date", "startedAt");
        }
        if (finishedAt.isBefore(startedAt)) {
            throw new ApiException("End date cannot be before start date", "finishedAt");
        }
        if (scheduleRepository.scheduleExistsInIntervalForDoctor(startedAt,
                finishedAt, doctorId)) {
            throw new ApiException("Schedule overlaps with already existing one", "schedule");
        }
        if (absenceRepository.absenceExistsInIntervalForDoctor(startedAt, finishedAt, doctorId)) {
            throw new ApiException("Schedule overlaps with already existing absence", "absence");
        }
    }

    private void validateNewTimeSlot(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId, Schedule schedule) {
        if (!Objects.equals(startedAt.getDayOfMonth(), finishedAt.getDayOfMonth())) {
            throw new ApiException("Schedule can be one day long", "startedAt, finishedAt");
        }
        if (startedAt.getHour() < startOfDay) {
            throw new ApiException("Start date cannot be before work hours", "startedAt");
        }
        if (finishedAt.getHour() > endOfDay) {
            throw new ApiException("End date cannot be after work hours", "finishedAt");
        }
        if (startedAt.isAfter(finishedAt)) {
            throw new ApiException("Start date cannot be after end date", "startedAt");
        }
        if (finishedAt.isBefore(startedAt)) {
            throw new ApiException("End date cannot be before start date", "finishedAt");
        }
        if (scheduleRepository.scheduleExistsInIntervalForDoctorExcludingId(startedAt,
                finishedAt, doctorId, schedule.getId())) {
            throw new ApiException("Schedule overlaps with already existing one", "schedule");
        }
        if (absenceRepository.absenceExistsInIntervalForDoctor(startedAt, finishedAt, doctorId)) {
            throw new ApiException("Schedule overlaps with already existing absence", "absence");
        }
    }
    

}
