package pl.edu.pja.aurorumclinic.features.appointments.surveys.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.edu.pja.aurorumclinic.features.appointments.shared.SurveyRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentFinishedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.SurveyCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Survey;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateSurvey {

    private final SurveyRepository surveyRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAppointmentFinishedEvent(AppointmentFinishedEvent event) {
        Appointment appointment = event.appointment();
        Survey survey = Survey.builder()
                .createdAt(LocalDateTime.now())
                .appointment(appointment)
                .build();
        Survey savedSurvey = surveyRepository.save(survey);
        applicationEventPublisher.publishEvent(new SurveyCreatedEvent(savedSurvey));
    }

}
