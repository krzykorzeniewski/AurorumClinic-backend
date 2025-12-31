package pl.edu.pja.aurorumclinic.features.opinions.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/patients/me/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientDeleteOpinion {

    private final AppointmentRepository appointmentRepository;
    private final OpinionRepository opinionRepository;

    @DeleteMapping("/{appointmentId}/opinion")
    @Transactional
    public ResponseEntity<ApiResponse<Boolean>> delete(
            @AuthenticationPrincipal Long userID,
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(userID, appointmentId)));
    }

    private Boolean handle(Long userID, Long appointmentId) {

        Appointment appt = appointmentRepository.getAppointmentByIdAndPatientId(appointmentId, userID);
        if (appt == null) {
            throw new ApiNotFoundException(
                    "You can delete only your own opinion",
                    "appointmentId"
            );
        }

        if (appt.getOpinion() == null) {
            throw new ApiNotFoundException("Opinion not found", "opinionId");
        }

        opinionRepository.delete(appt.getOpinion());

        appt.setOpinion(null);

        return true;
    }
}
