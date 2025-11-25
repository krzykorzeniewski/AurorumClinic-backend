package pl.edu.pja.aurorumclinic.features.schedules.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.schedules.ScheduleValidator;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeCreateWeeklySchedule {

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;
    private final ScheduleValidator scheduleValidator;

    @PostMapping("/weekly")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createWeeklySchedule(
            @RequestBody @Valid EmpCreateWeeklyScheduleRequest request) {
        handle(request, request.doctorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(EmpCreateWeeklyScheduleRequest request, Long doctorId) {
        if (request.finishedAt.isBefore(request.startedAt)) {
            throw new ApiException("finishedAt date is before startedAt", "finishedAt");
        }
        if (request.startedAt.isBefore(LocalDate.now()) && request.finishedAt.isBefore(LocalDate.now())) {
            throw new ApiException("finishedAt and startedAt are both in the past", "finishedAt, startedAt");
        }
        Doctor doctorFromDb = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        List<Service> mondayServicesFromDb = serviceRepository.findAllById(request.mon.serviceIds);
        if (!mondayServicesFromDb.stream().map(Service::getId).collect(Collectors.toSet()).containsAll(request.mon.serviceIds)) {
            throw new ApiException("Monday service ids are not found", "monServiceIds");
        }
        List<Service> tuesdayServicesFromDb = serviceRepository.findAllById(request.tue.serviceIds);
        if (!tuesdayServicesFromDb.stream().map(Service::getId).collect(Collectors.toSet()).containsAll(request.tue.serviceIds)) {
            throw new ApiException("Tuesday service ids are not found", "tueServiceIds");
        }
        List<Service> wednesdayServicesFromDb = serviceRepository.findAllById(request.wed.serviceIds);
        if (!wednesdayServicesFromDb.stream().map(Service::getId).collect(Collectors.toSet()).containsAll(request.wed.serviceIds)) {
            throw new ApiException("Wednesday service ids are not found", "wedServiceIds");
        }
        List<Service> thursdayServicesFromDb = serviceRepository.findAllById(request.thu.serviceIds);
        if (!thursdayServicesFromDb.stream().map(Service::getId).collect(Collectors.toSet()).containsAll(request.thu.serviceIds)) {
            throw new ApiException("Thursday service ids are not found", "thuServiceIds");
        }
        List<Service> fridayServicesFromDb = serviceRepository.findAllById(request.fri.serviceIds);
        if (!fridayServicesFromDb.stream().map(Service::getId).collect(Collectors.toSet()).containsAll(request.fri.serviceIds)) {
            throw new ApiException("Friday service ids are not found", "friServiceIds");
        }
        LocalDate todayDateTime = LocalDate.now();
        LocalDate scheduleStartDateTime = request.startedAt;
        LocalDate scheduleFinishDateTime = request.finishedAt;
        LocalDate currentDateTime =
                todayDateTime.isAfter(scheduleStartDateTime) ? todayDateTime : scheduleStartDateTime;

        while(!currentDateTime.isAfter(scheduleFinishDateTime)) {
            DayOfWeek currentDay = currentDateTime.getDayOfWeek();
            try {
                if (Objects.equals(currentDay, DayOfWeek.SATURDAY) || Objects.equals(currentDay, DayOfWeek.SUNDAY)) {
                    currentDateTime = currentDateTime.plusDays(1);
                    continue;
                } else if (Objects.equals(currentDay, DayOfWeek.MONDAY)) {
                    LocalDateTime startedAt = LocalDateTime.of(currentDateTime, request.mon.hours.get(0));
                    LocalDateTime finishedAt = LocalDateTime.of(currentDateTime, request.mon.hours.get(1));
                    scheduleValidator.validateTimeslotAndServices(startedAt, finishedAt, doctorFromDb, mondayServicesFromDb);
                    Schedule mondaySchedule = Schedule.builder()
                            .doctor(doctorFromDb)
                            .startedAt(startedAt)
                            .finishedAt(finishedAt)
                            .services(new HashSet<>(mondayServicesFromDb))
                            .build();
                    scheduleRepository.save(mondaySchedule);
                } else if (Objects.equals(currentDay, DayOfWeek.TUESDAY)) {
                    LocalDateTime startedAt = LocalDateTime.of(currentDateTime, request.tue.hours.get(0));
                    LocalDateTime finishedAt = LocalDateTime.of(currentDateTime, request.tue.hours.get(1));
                    scheduleValidator.validateTimeslotAndServices(startedAt, finishedAt, doctorFromDb, tuesdayServicesFromDb);
                    Schedule tuesdaySchedule = Schedule.builder()
                            .doctor(doctorFromDb)
                            .startedAt(startedAt)
                            .finishedAt(finishedAt)
                            .services(new HashSet<>(tuesdayServicesFromDb))
                            .build();
                    scheduleRepository.save(tuesdaySchedule);
                } else if (Objects.equals(currentDay, DayOfWeek.WEDNESDAY)) {
                    LocalDateTime startedAt = LocalDateTime.of(currentDateTime, request.wed.hours.get(0));
                    LocalDateTime finishedAt = LocalDateTime.of(currentDateTime, request.wed.hours.get(1));
                    scheduleValidator.validateTimeslotAndServices(startedAt, finishedAt, doctorFromDb, wednesdayServicesFromDb);
                    Schedule wednesdaySchedule = Schedule.builder()
                            .doctor(doctorFromDb)
                            .startedAt(startedAt)
                            .finishedAt(finishedAt)
                            .services(new HashSet<>(wednesdayServicesFromDb))
                            .build();
                    scheduleRepository.save(wednesdaySchedule);
                } else if (Objects.equals(currentDay, DayOfWeek.THURSDAY)) {
                    LocalDateTime startedAt = LocalDateTime.of(currentDateTime, request.thu.hours.get(0));
                    LocalDateTime finishedAt = LocalDateTime.of(currentDateTime, request.thu.hours.get(1));
                    scheduleValidator.validateTimeslotAndServices(startedAt, finishedAt, doctorFromDb, thursdayServicesFromDb);
                    Schedule thursdaySchedule = Schedule.builder()
                            .doctor(doctorFromDb)
                            .startedAt(startedAt)
                            .finishedAt(finishedAt)
                            .services(new HashSet<>(thursdayServicesFromDb))
                            .build();
                    scheduleRepository.save(thursdaySchedule);
                } else if (Objects.equals(currentDay, DayOfWeek.FRIDAY)) {
                    LocalDateTime startedAt = LocalDateTime.of(currentDateTime, request.fri.hours.get(0));
                    LocalDateTime finishedAt = LocalDateTime.of(currentDateTime, request.fri.hours.get(1));
                    scheduleValidator.validateTimeslotAndServices(startedAt, finishedAt, doctorFromDb, fridayServicesFromDb);
                    Schedule fridaySchedule = Schedule.builder()
                            .doctor(doctorFromDb)
                            .startedAt(startedAt)
                            .finishedAt(finishedAt)
                            .services(new HashSet<>(fridayServicesFromDb))
                            .build();
                    scheduleRepository.save(fridaySchedule);
                }
            } catch (ApiException e) {
                if (Objects.equals(e.getField(), "absence")) {
                    currentDateTime = currentDateTime.plusDays(1);
                    continue;
                } else {
                    throw new ApiException(e.getMessage(), e.getField());
                }
            }
            currentDateTime = currentDateTime.plusDays(1);
        }
    }

    @Builder
    record EmpCreateWeeklyScheduleRequest(@NotNull DayDto mon,
                                          @NotNull DayDto tue,
                                          @NotNull DayDto wed,
                                          @NotNull DayDto thu,
                                          @NotNull DayDto fri,
                                          @NotNull LocalDate startedAt,
                                          @NotNull LocalDate finishedAt,
                                          @NotNull Long doctorId) {
        @Builder
        record DayDto(@NotEmpty @Size(min = 2, max = 2) List<LocalTime> hours,
                      @NotNull Set<Long> serviceIds) {
        }
    }
}

