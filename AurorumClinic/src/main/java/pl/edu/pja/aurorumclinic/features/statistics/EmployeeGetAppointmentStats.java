package pl.edu.pja.aurorumclinic.features.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Tuple;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/stats/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeGetAppointmentStats {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping
    public ResponseEntity<ApiResponse<AllAppointmentStatsResponse>> getAppointmentStatistics(
            @RequestParam LocalDateTime startedAt,
            @RequestParam LocalDateTime finishedAt,
            @RequestParam(required = false) String fetch
            ) {
        return ResponseEntity.ok(ApiResponse.success(handle(startedAt, finishedAt, fetch)));
    }

    private AllAppointmentStatsResponse handle(LocalDateTime startedAt, LocalDateTime finishedAt, String fetch) {
        List<Tuple> allAppointmentStatsBetween = appointmentRepository
                .getAllAppointmentStatsBetween(startedAt, finishedAt);

        if (Objects.equals(fetch, "all")) {
            AllAppointmentStatsResponse.AllAppointmentStatsResponseBuilder allAppointmentStatsResponseBuilder = AllAppointmentStatsResponse.builder()
                    .totalScheduled((Long) allAppointmentStatsBetween.get(0).get("scheduled"))
                    .totalFinished((Long) allAppointmentStatsBetween.get(0).get("finished"))
                    .avgDuration((Double) allAppointmentStatsBetween.get(0).get("avgDuration"))
                    .avgRating((Double) allAppointmentStatsBetween.get(0).get("avgRating"))
                    .doctors(new ArrayList<>());
            List<Doctor> doctorsFromDb = doctorRepository.findAll();
            for (Doctor doctor : doctorsFromDb) {
                List<Tuple> doctorTotalAppointmentStats = appointmentRepository
                        .getDoctorAppointmentStatsBetween(doctor.getId(), startedAt, finishedAt);
                List<Tuple> doctorAppointmentsStatsByService = appointmentRepository
                        .getDoctorAppointmentStatsPerServiceBetween(doctor.getId(), startedAt, finishedAt);
                allAppointmentStatsResponseBuilder.doctors.add(AllAppointmentStatsResponse.AppointmentStatsPerDoctorDto
                        .builder()
                        .doctorId(doctor.getId())
                        .name(doctor.getName())
                        .surname(doctor.getSurname())
                        .profilePicture(objectStorageService.generateUrl(doctor.getProfilePicture()))
                        .specializations(doctor.getSpecializations().stream().map(specialization -> AllAppointmentStatsResponse.AppointmentStatsPerDoctorDto.SpecializationDto.builder()
                                .id(specialization.getId())
                                .name(specialization.getName())
                                .build()).toList())
                        .total(List.of(AllAppointmentStatsResponse.AppointmentStatsPerDoctorDto.DoctorAppointmentStats.builder()
                                .totalScheduled((Long) doctorTotalAppointmentStats.get(0).get("scheduled"))
                                .totalFinished((Long) doctorTotalAppointmentStats.get(0).get("finished"))
                                .avgDuration((Double) doctorTotalAppointmentStats.get(0).get("avgDuration"))
                                .avgRating((Double) doctorTotalAppointmentStats.get(0).get("avgRating"))
                                .services(doctorAppointmentsStatsByService.stream().map(tuple ->
                                                AllAppointmentStatsResponse.AppointmentStatsPerDoctorDto.DoctorAppointmentStats.ServiceAppointmentStatsDto.builder()
                                                        .scheduled((Long) tuple.get("scheduled"))
                                                        .finished((Long) tuple.get("finished"))
                                                        .avgDuration((Double) tuple.get("avgDuration"))
                                                        .avgRating((Double) tuple.get("avgRating"))
                                                        .service(AllAppointmentStatsResponse.AppointmentStatsPerDoctorDto
                                                                .DoctorAppointmentStats.ServiceAppointmentStatsDto.ServiceDto.builder()
                                                                .id((Long) tuple.get("servId"))
                                                                .name((String) tuple.get("servName"))
                                                                .build())
                                                        .build())
                                        .toList())
                                .build()))
                        .build());
            }
            return allAppointmentStatsResponseBuilder.build();
        } else {
            return AllAppointmentStatsResponse.builder()
                    .totalScheduled((Long) allAppointmentStatsBetween.get(0).get("scheduled"))
                    .totalFinished((Long) allAppointmentStatsBetween.get(0).get("finished"))
                    .avgDuration((Double) allAppointmentStatsBetween.get(0).get("avgDuration"))
                    .avgRating((Double) allAppointmentStatsBetween.get(0).get("avgRating"))
                    .doctors(null)
                    .build();
        }
    }

    @Builder
    record AllAppointmentStatsResponse(Long totalScheduled,
                                       Long totalFinished,
                                       Double avgDuration,
                                       Double avgRating,
                                       @JsonInclude(JsonInclude.Include.NON_NULL) List<AppointmentStatsPerDoctorDto> doctors) {
        @Builder
        record AppointmentStatsPerDoctorDto(Long doctorId,
                                            String name,
                                            String surname,
                                            String profilePicture,
                                            List<SpecializationDto> specializations,
                                            List<DoctorAppointmentStats> total) {
            @Builder
            record SpecializationDto(Long id,
                                     String name) {
            }

            @Builder
            record DoctorAppointmentStats(Long totalScheduled,
                                                  Long totalFinished,
                                                  Double avgDuration,
                                                  Double avgRating,
                                                  List<ServiceAppointmentStatsDto> services) {
                @Builder
                record ServiceAppointmentStatsDto(Long scheduled,
                                                  Long finished,
                                                  Double avgDuration,
                                                  Double avgRating,
                                                  ServiceAppointmentStatsDto.ServiceDto service) {
                    @Builder
                    record ServiceDto(Long id,
                                      String name) {

                    }
                }
            }
        }
    }

}
