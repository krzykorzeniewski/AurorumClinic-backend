package pl.edu.pja.aurorumclinic.features.appointments.surveys.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.appointments.shared.data.SurveyRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Survey;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Objects;

@RestController
@RequestMapping("/api/surveys/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class ExistsById {

    private final SurveyRepository surveyRepository;

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<ApiResponse<?>> updateSurvey(@PathVariable("id") Long surveyId,
                                                       @AuthenticationPrincipal Long userId) {
        handle(surveyId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(Long surveyId, Long userId) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!Objects.equals(userId, survey.getAppointment().getPatient().getId())) {
            throw new ApiAuthorizationException("user id doest not match patient id");
        }
        if (survey.getCompletedAt() != null) {
            throw new ApiException("Survey is already completed", "completedAt");
        }
    }

}
