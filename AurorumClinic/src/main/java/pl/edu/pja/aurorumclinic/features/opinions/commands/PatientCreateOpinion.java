package pl.edu.pja.aurorumclinic.features.opinions.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.moderation.ContentModerationService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/patients/me/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientCreateOpinion {

    private final AppointmentRepository appointmentRepository;
    private final OpinionRepository opinionRepository;
    private final ContentModerationService contentModerationService;

    public record Request(
            @Min(1) @Max(5) int rating,
            @NotBlank @Size(max = 2000) String comment
    ) {}

    @Builder
    public record Response(Long opinionId, int rating, String comment,
                           LocalDateTime createdAt, Long appointmentId, Long doctorId) {}

    @PostMapping("/{appointmentId}/opinion")
    @Transactional
    public ResponseEntity<ApiResponse<Response>> create(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long appointmentId,
            @RequestBody @Valid Request req
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(userId, appointmentId, req)));
    }

    private Response handle(Long userId, Long appointmentId, Request req) {
        contentModerationService.assertAllowed(req.comment(), "comment");

        Appointment appt = appointmentRepository.getAppointmentByIdAndPatientId(appointmentId, userId);
        if (appt == null) {
            throw new ApiNotFoundException("You can only rate your own appointment", "appointmentId");
        }

        if (appt.getStatus() != AppointmentStatus.FINISHED) {
            throw new ApiConflictException("You can rate only finished appointments", "appointmentId");
        }

        if (appt.getOpinion() != null) {
            throw new ApiConflictException("Opinion already exists for this appointment", "appointmentId");
        }

        Opinion op = Opinion.builder()
                .rating(req.rating())
                .comment(req.comment())
                .createdAt(LocalDateTime.now())
                .build();

        opinionRepository.save(op);
        appt.setOpinion(op);

        return Response.builder()
                .opinionId(op.getId())
                .rating(op.getRating())
                .comment(op.getComment())
                .createdAt(op.getCreatedAt())
                .appointmentId(appt.getId())
                .doctorId(appt.getDoctor() != null ? appt.getDoctor().getId() : null)
                .build();
    }
}
