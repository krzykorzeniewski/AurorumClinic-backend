package pl.edu.pja.aurorumclinic.features.schedules.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.schedules.ScheduleValidator;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeUpdateSchedule {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleValidator scheduleValidator;
    private final ServiceRepository serviceRepository;

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> updateSchedule(@PathVariable("id") Long scheduleId,
                                                         @RequestBody @Valid EmpUpdateScheduleRequest request) {
        handle(scheduleId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long scheduleId, EmpUpdateScheduleRequest request) {
        Schedule scheduleFromDb = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        List<Service> servicesFromDb = serviceRepository.findAllById(request.serviceIds);
        if (servicesFromDb.size() > request.serviceIds.size()) {
            throw new ApiException("Some service ids are not found", "serviceIds");
        }
        scheduleValidator.checkIfScheduleHasAppointments(scheduleFromDb);
        scheduleValidator.validateTimeslotAndServices(request.startedAt, request.finishedAt, scheduleFromDb.getDoctor(),
                servicesFromDb);

        scheduleFromDb.setStartedAt(request.startedAt);
        scheduleFromDb.setFinishedAt(request.finishedAt);
        scheduleFromDb.setServices(new HashSet<>(servicesFromDb));
    }

    record EmpUpdateScheduleRequest(@NotNull LocalDateTime startedAt,
                                    @NotNull LocalDateTime finishedAt,
                                    @NotNull Set<Long> serviceIds) {
    }

}
