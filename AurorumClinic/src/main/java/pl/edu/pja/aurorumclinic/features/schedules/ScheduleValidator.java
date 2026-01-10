package pl.edu.pja.aurorumclinic.features.schedules;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ScheduleValidator {

    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AbsenceRepository absenceRepository;
    private final ServiceRepository serviceRepository;

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
        if (appointmentRepository.existsBySchedule(
                schedule.getDoctor().getId(), schedule.getStartedAt(), schedule.getFinishedAt())) {
            Set<Long> appointmentIdsInTimeslot = appointmentRepository.getAppointmentIdsInScheduleTimeslot(
                    schedule.getDoctor().getId(), schedule.getStartedAt(), schedule.getFinishedAt());
            throw new ApiException(appointmentIdsInTimeslot.toString(), "appointments");
        }
    }

    public void checkIfScheduleHasAppointmentsInOldTimeslot(Schedule schedule,
                                                            LocalDateTime newStartedAt, LocalDateTime newFinishedAt) {
        LocalDateTime oldStartedAt = schedule.getStartedAt();
        LocalDateTime oldFinishedAt = schedule.getFinishedAt();
        if ((newStartedAt.isBefore(oldStartedAt) || newStartedAt.isEqual(oldStartedAt))
                && (newFinishedAt.isAfter(oldFinishedAt) || newFinishedAt.isEqual(oldFinishedAt))) {
            return;
        }
        Set<Long> appointmentIdsInTimeslot = appointmentRepository.getAppointmentIdsInPreviousScheduleTimeslot
                (schedule.getDoctor().getId(), oldStartedAt, oldFinishedAt, newStartedAt, newFinishedAt);
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
        validateDates(startedAt, finishedAt);
        if (scheduleRepository.scheduleExistsInIntervalForDoctor(startedAt,
                finishedAt, doctorId)) {
            throw new ApiException("Schedule overlaps with already existing one", "schedule");
        }
        if (absenceRepository.absenceExistsInIntervalForDoctor(startedAt, finishedAt, doctorId)) {
            throw new ApiException("Schedule overlaps with already existing absence", "absence");
        }
    }

    private void validateNewTimeSlot(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId, Schedule schedule) {
        validateDates(startedAt, finishedAt);
        if (scheduleRepository.scheduleExistsInIntervalForDoctorExcludingId(startedAt,
                finishedAt, doctorId, schedule.getId())) {
            throw new ApiException("Schedule overlaps with already existing one", "schedule");
        }
        if (absenceRepository.absenceExistsInIntervalForDoctor(startedAt, finishedAt, doctorId)) {
            throw new ApiException("Schedule overlaps with already existing absence", "absence");
        }
    }

    private void validateDates(LocalDateTime startedAt, LocalDateTime finishedAt) {
        if (startedAt.isAfter(finishedAt) || startedAt.equals(finishedAt)) {
            throw new ApiException("Start date must be before end date", "startedAt");
        }
        if (startedAt.isBefore(LocalDateTime.now())) {
            throw new ApiException("Start date cannot be in the past", "startedAt");
        }
        if (!startedAt.toLocalDate().equals(finishedAt.toLocalDate())) {
            throw new ApiException("Schedule can be one day long", "startedAt, finishedAt");
        }
        if (startedAt.getHour() < startOfDay) {
            throw new ApiException("Start date cannot be before work hours", "startedAt");
        }
        if (finishedAt.getHour() > endOfDay) {
            throw new ApiException("End date cannot be after work hours", "finishedAt");
        }
        if (Objects.equals(startedAt.getDayOfWeek(), DayOfWeek.SATURDAY) ||
                Objects.equals(startedAt.getDayOfWeek(), DayOfWeek.SUNDAY)) {
            throw new ApiException("Weekends are free of work", "startedAt");
        }
        int minDuration = serviceRepository.getMinServiceDuration();
        if (ChronoUnit.MINUTES.between(startedAt, finishedAt) < minDuration) {
            throw new ApiException("Schedule has to last for at least the minimum service duration", "startedAt, finishedAt");
        }
    }

}
