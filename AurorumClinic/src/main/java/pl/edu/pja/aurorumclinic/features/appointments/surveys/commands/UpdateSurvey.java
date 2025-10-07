package pl.edu.pja.aurorumclinic.features.appointments.surveys.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class UpdateSurvey {
}
