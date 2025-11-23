package pl.edu.pja.aurorumclinic.features.schedules.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorCreateWeeklySchedule {

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;
    private final ScheduleValidator scheduleValidator;

    @PostMapping("/weekly/me")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createWeeklySchedule(@RequestBody @Valid DoctorCreateWeeklySchedule.DocCreateWeeklyScheduleRequest request,
                                                               @AuthenticationPrincipal Long doctorId) {
        handle(request, doctorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(DocCreateWeeklyScheduleRequest request, Long doctorId) {
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
        if (mondayServicesFromDb.size() > request.mon.serviceIds.size()) { //TODO dodac check jak w create schedule
            throw new ApiException("Some service ids are not found", "monServiceIds");
        }
        List<Service> tuesdayServicesFromDb = serviceRepository.findAllById(request.tue.serviceIds);
        if (tuesdayServicesFromDb.size() > request.tue.serviceIds.size()) {
            throw new ApiException("Some service ids are not found", "tueServiceIds");
        }
        List<Service> wednesdayServicesFromDb = serviceRepository.findAllById(request.wed.serviceIds);
        if (wednesdayServicesFromDb.size() > request.wed.serviceIds.size()) {
            throw new ApiException("Some service ids are not found", "wedServiceIds");
        }
        List<Service> thursdayServicesFromDb = serviceRepository.findAllById(request.thu.serviceIds);
        if (thursdayServicesFromDb.size() > request.thu.serviceIds.size()) {
            throw new ApiException("Some service ids are not found", "thuServiceIds");
        }
        List<Service> fridayServicesFromDb = serviceRepository.findAllById(request.fri.serviceIds);
        if (fridayServicesFromDb.size() > request.fri.serviceIds.size()) {
            throw new ApiException("Some service ids are not found", "friServiceIds");
        }
        LocalDate todayDate = LocalDate.now();
        LocalDate scheduleStartDate = request.startedAt;
        LocalDate scheduleFinishDate = request.finishedAt;
        LocalDate currentDate =
                todayDate.isAfter(scheduleStartDate) ? todayDate : scheduleStartDate;

        while(!currentDate.isAfter(scheduleFinishDate)) {
            DayOfWeek currentDay = currentDate.getDayOfWeek();
            try {
                if (Objects.equals(currentDay, DayOfWeek.SATURDAY) || Objects.equals(currentDay, DayOfWeek.SUNDAY)) {
                    currentDate = currentDate.plusDays(1);
                    continue;
                } else if (Objects.equals(currentDay, DayOfWeek.MONDAY)) {
                    LocalDateTime startedAt = LocalDateTime.of(currentDate, request.mon.hours.get(0));
                    LocalDateTime finishedAt = LocalDateTime.of(currentDate, request.mon.hours.get(1));
                    scheduleValidator.validateTimeslotAndServices(startedAt, finishedAt, doctorFromDb, mondayServicesFromDb);
                    Schedule mondaySchedule = Schedule.builder()
                            .doctor(doctorFromDb)
                            .startedAt(startedAt)
                            .finishedAt(finishedAt)
                            .services(new HashSet<>(mondayServicesFromDb))
                            .build();
                    scheduleRepository.save(mondaySchedule);
                } else if (Objects.equals(currentDay, DayOfWeek.TUESDAY)) {
                    LocalDateTime startedAt = LocalDateTime.of(currentDate, request.tue.hours.get(0));
                    LocalDateTime finishedAt = LocalDateTime.of(currentDate, request.tue.hours.get(1));
                    scheduleValidator.validateTimeslotAndServices(startedAt, finishedAt, doctorFromDb, tuesdayServicesFromDb);
                    Schedule tuesdaySchedule = Schedule.builder()
                            .doctor(doctorFromDb)
                            .startedAt(startedAt)
                            .finishedAt(finishedAt)
                            .services(new HashSet<>(tuesdayServicesFromDb))
                            .build();
                    scheduleRepository.save(tuesdaySchedule);
                } else if (Objects.equals(currentDay, DayOfWeek.WEDNESDAY)) {
                    LocalDateTime startedAt = LocalDateTime.of(currentDate, request.wed.hours.get(0));
                    LocalDateTime finishedAt = LocalDateTime.of(currentDate, request.wed.hours.get(1));
                    scheduleValidator.validateTimeslotAndServices(startedAt, finishedAt, doctorFromDb, wednesdayServicesFromDb);
                    Schedule wednesdaySchedule = Schedule.builder()
                            .doctor(doctorFromDb)
                            .startedAt(startedAt)
                            .finishedAt(finishedAt)
                            .services(new HashSet<>(wednesdayServicesFromDb))
                            .build();
                    scheduleRepository.save(wednesdaySchedule);
                } else if (Objects.equals(currentDay, DayOfWeek.THURSDAY)) {
                    LocalDateTime startedAt = LocalDateTime.of(currentDate, request.thu.hours.get(0));
                    LocalDateTime finishedAt = LocalDateTime.of(currentDate, request.thu.hours.get(1));
                    scheduleValidator.validateTimeslotAndServices(startedAt, finishedAt, doctorFromDb, thursdayServicesFromDb);
                    Schedule thursdaySchedule = Schedule.builder()
                            .doctor(doctorFromDb)
                            .startedAt(startedAt)
                            .finishedAt(finishedAt)
                            .services(new HashSet<>(thursdayServicesFromDb))
                            .build();
                    scheduleRepository.save(thursdaySchedule);
                } else if (Objects.equals(currentDay, DayOfWeek.FRIDAY)) {
                    LocalDateTime startedAt = LocalDateTime.of(currentDate, request.fri.hours.get(0));
                    LocalDateTime finishedAt = LocalDateTime.of(currentDate, request.fri.hours.get(1));
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
                    currentDate = currentDate.plusDays(1);
                    continue;
                }
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    @Builder
    record DocCreateWeeklyScheduleRequest(@NotNull DayDto mon,
                                          @NotNull DayDto tue,
                                          @NotNull DayDto wed,
                                          @NotNull DayDto thu,
                                          @NotNull DayDto fri,
                                          @NotNull LocalDate startedAt,
                                          @NotNull LocalDate finishedAt) {
        @Builder
        record DayDto(@NotEmpty @Size(min = 2, max = 2) List<LocalTime> hours,
                      @NotNull Set<Long> serviceIds) {
        }
    }
}
