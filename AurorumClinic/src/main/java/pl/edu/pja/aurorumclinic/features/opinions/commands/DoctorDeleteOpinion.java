package pl.edu.pja.aurorumclinic.features.opinions.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/doctors/me/opinions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorDeleteOpinion {

    private final OpinionRepository opinionRepository;

    @DeleteMapping("/{opinionId}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> delete(
            @AuthenticationPrincipal Long doctorId,
            @PathVariable Long opinionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, opinionId)));
    }

    private String handle(Long doctorId, Long opinionId) {
        Opinion op = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new ApiNotFoundException("Opinion not found", "opinionId"));

        if (!op.getAppointment().getDoctor().getId().equals(doctorId)) {
            throw new ApiAuthorizationException("You cannot delete someone else's opinion");
        }

        opinionRepository.delete(op);
        return "Opinion deleted successfully";
    }
}
