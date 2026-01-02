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
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Objects;

@RestController
@RequestMapping("/api/patients/me/opinions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientDeleteOpinion {

    private final OpinionRepository opinionRepository;

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Boolean>> delete(
            @AuthenticationPrincipal Long userID,
            @PathVariable("id") Long opinionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(userID, opinionId)));
    }

    private Boolean handle(Long userID, Long opinionId) {

        Opinion opinionFromDb = opinionRepository.findById(opinionId).orElseThrow(
                () -> new ApiNotFoundException("Opinion not found", "opinionId")
        );
        if (!Objects.equals(opinionFromDb.getAppointment().getPatient().getId(), userID)) {
            throw new ApiAuthorizationException("Opinions appointment is not assigned to this user");
        }
        opinionFromDb.getAppointment().setOpinion(null);
        opinionRepository.delete(opinionFromDb);
        return true;
    }
}
