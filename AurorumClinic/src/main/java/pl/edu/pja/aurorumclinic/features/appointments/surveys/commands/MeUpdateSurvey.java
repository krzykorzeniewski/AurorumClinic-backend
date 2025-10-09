package pl.edu.pja.aurorumclinic.features.appointments.surveys.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.appointments.shared.SurveyRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Survey;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/surveys/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class MeUpdateSurvey {

    private final SurveyRepository surveyRepository;

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> updateSurvey(@PathVariable("id") Long surveyId,
                                                       @RequestBody @Valid UpdateSurveyRequest request,
                                                       @AuthenticationPrincipal Long userId) {
        handle(surveyId, request, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long surveyId, UpdateSurveyRequest request, Long userId) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!Objects.equals(userId, survey.getAppointment().getPatient().getId())) {
            throw new ApiAuthorizationException("user id doest not match patient id");
        }
        if (survey.getCompletedAt() != null) {
            throw new ApiException("Survey is already completed", "completedAt");
        }
        survey.setGrade(request.grade);
        survey.setComment(request.comment);
        survey.setCompletedAt(LocalDateTime.now());
    }

    public record UpdateSurveyRequest(@Min(1) @Max(5) @NotNull Integer grade,
                                      @Size(max = 300) String comment) {

    }

}
