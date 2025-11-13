package pl.edu.pja.aurorumclinic.features.schedules.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.schedules.ScheduleValidator;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Doctor')")
public class DoctorUpdateSchedule {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleValidator scheduleValidator;
    private final ServiceRepository serviceRepository;

    @PutMapping("/me/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> updateSchedule(@PathVariable("id") Long scheduleId,
                                                         @RequestBody @Valid DoctorUpdateSchedule.DocUpdateScheduleRequest request,
                                                         @AuthenticationPrincipal Long doctorId) {
        handle(scheduleId, request, doctorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long scheduleId, DocUpdateScheduleRequest request, Long doctorId) {
        Schedule scheduleFromDb = scheduleRepository.findById(scheduleId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!Objects.equals(scheduleFromDb.getDoctor().getId(), doctorId)) {
            throw new ApiAuthorizationException("doctor id is not assigned to this schedule");
        }
        List<Service> servicesFromDb = serviceRepository.findAllById(request.serviceIds);
        if (servicesFromDb.size() > request.serviceIds.size()) {
            throw new ApiException("Some service ids are not found", "serviceIds");
        }
        scheduleValidator.validateNewTimeslotAndServices(request.startedAt, request.finishedAt, scheduleFromDb.getDoctor(),
                servicesFromDb, scheduleFromDb);
        scheduleValidator.checkIfScheduleHasAppointmentsInOldTimeslot(scheduleFromDb, request.startedAt, request.finishedAt);

        scheduleFromDb.setStartedAt(request.startedAt);
        scheduleFromDb.setFinishedAt(request.finishedAt);
        scheduleFromDb.setServices(new HashSet<>(servicesFromDb));
    }

    record DocUpdateScheduleRequest(@NotNull LocalDateTime startedAt,
                                    @NotNull LocalDateTime finishedAt,
                                    @NotNull Set<Long> serviceIds) {
    }

}
