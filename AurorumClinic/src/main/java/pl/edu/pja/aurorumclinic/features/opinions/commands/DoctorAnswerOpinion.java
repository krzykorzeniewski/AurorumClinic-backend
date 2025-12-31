package pl.edu.pja.aurorumclinic.features.opinions.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
import pl.edu.pja.aurorumclinic.shared.moderation.ContentModerationService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/doctors/me/opinions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorAnswerOpinion {

    private final OpinionRepository opinionRepository;
    private final ContentModerationService contentModerationService;

    public record Request(@NotBlank @Size(max = 2000) String answer) {}

    @PatchMapping("/{opinionId}/answer")
    @Transactional
    public ResponseEntity<ApiResponse<String>> answer(
            @AuthenticationPrincipal Long doctorId,
            @PathVariable Long opinionId,
            @RequestBody @Valid Request req
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, opinionId, req)));
    }

    private String handle(Long doctorId, Long opinionId, Request req) {
        Opinion op = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new ApiNotFoundException("Opinion not found", "opinionId"));
        contentModerationService.assertAllowed(req.answer(), "answer");

        if (!op.getAppointment().getDoctor().getId().equals(doctorId)) {
            throw new ApiAuthorizationException("You cannot answer an opinion for another doctor");
        }

        op.setAnswer(req.answer());
        op.setAnsweredAt(LocalDateTime.now());
        return "Answer added successfully";
    }
}
