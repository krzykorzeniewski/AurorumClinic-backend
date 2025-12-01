package pl.edu.pja.aurorumclinic.features.users.doctors.queries;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class GetDoctorByIdAppointmentSlots {

    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;

    @PermitAll
    @GetMapping("/{id}/appointment-slots")
    public ResponseEntity<ApiResponse<List<LocalDateTime>>> getAppointmentSlots(@PathVariable("id") Long doctorId,
                                                                   @RequestParam LocalDateTime startedAt,
                                                                   @RequestParam LocalDateTime finishedAt,
                                                                   @RequestParam Long serviceId) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, startedAt, finishedAt, serviceId)));
    }

    private List<LocalDateTime> handle(Long doctorId, LocalDateTime startedAt, LocalDateTime finishedAt, Long serviceId) {
        List<LocalDateTime> responseList = new ArrayList<>();
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("doctorId not found", "id")
        );
        List<Schedule> doctorSchedules = doctor.getSchedules().stream().filter(schedule ->
                        schedule.getFinishedAt().isAfter(startedAt) && schedule.getStartedAt().isBefore(finishedAt))
                .toList();
        Service serviceFromDb = serviceRepository.findById(serviceId).orElseThrow(
                () -> new ApiNotFoundException("serviceId not found", "id")
        );

        int specCounter = 0;
        for (Specialization specialization: doctor.getSpecializations()) {
            if (specialization.getServices().contains(serviceFromDb)) {
                specCounter++;
            }
        }
        if (specCounter == 0) {
            throw new ApiException("Doctor specialization is not assigned to this service", "specialization");
        }

        int duration = serviceFromDb.getDuration();

        for (Schedule schedule : doctorSchedules) {
            LocalDateTime appointmentSlotStart = schedule.getStartedAt().isBefore(startedAt)
                    ? startedAt : schedule.getStartedAt();

            LocalDateTime effectiveScheduleEnd = schedule.getFinishedAt().isAfter(finishedAt)
                    ? finishedAt : schedule.getFinishedAt();

            LocalDateTime appointmentSlotEnd = appointmentSlotStart.plusMinutes(duration);

            while (appointmentSlotStart.isBefore(effectiveScheduleEnd) &&
                    !appointmentSlotEnd.isAfter(effectiveScheduleEnd)) {

                if (!appointmentRepository.isTimeSlotAvailable(appointmentSlotStart, appointmentSlotEnd, doctor.getId(),
                        serviceFromDb.getId())) {
                    LocalDateTime finalAppointmentSlotEnd = appointmentSlotEnd;
                    LocalDateTime finalAppointmentSlotStart = appointmentSlotStart;
                    Appointment overlappingAppointment = doctor.getAppointments().stream()
                            .filter(appointment ->
                                    appointment.getStartedAt().isBefore(finalAppointmentSlotEnd) &&
                                            appointment.getFinishedAt().isAfter(finalAppointmentSlotStart))
                            .sorted(Comparator.comparing(Appointment::getFinishedAt).reversed())
                            .findFirst()
                            .orElse(null);
                    if (overlappingAppointment != null) {
                        appointmentSlotStart = overlappingAppointment.getFinishedAt();
                        appointmentSlotEnd = appointmentSlotStart.plusMinutes(duration);
                        continue;
                    }
                }

                responseList.add(appointmentSlotStart);
                appointmentSlotStart = appointmentSlotEnd;
                appointmentSlotEnd = appointmentSlotStart.plusMinutes(duration);
            }
        }
        Collections.sort(responseList);
        return responseList;
    }
}
