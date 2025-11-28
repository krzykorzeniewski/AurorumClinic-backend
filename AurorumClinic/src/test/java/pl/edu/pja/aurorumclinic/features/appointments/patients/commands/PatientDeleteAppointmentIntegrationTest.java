package pl.edu.pja.aurorumclinic.features.appointments.patients.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentDeletedEvent;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.test_config.IntegrationTest;
import pl.edu.pja.aurorumclinic.test_config.TestDataConfiguration;
import pl.edu.pja.aurorumclinic.test_config.WithMockCustomEmployee;
import pl.edu.pja.aurorumclinic.test_config.WithMockCustomUser;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
@RecordApplicationEvents
public class PatientDeleteAppointmentIntegrationTest extends IntegrationTest {

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    AppointmentRepository appointmentRepository;

    @Test
    @WithMockCustomEmployee
    void deleteAppointmentShouldReturn403WhenUserRoleIsNotPatient() {
        Long appointmentId = 1L;

        MvcTestResult result = mvcTester.delete()
                .uri("/api/appointments/me/{id}", appointmentId)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(result).bodyText().containsIgnoringCase("denied");
    }


    @Test
    @WithMockCustomUser
    void deleteAppointmentShouldReturn404WhenAppointmentIdIsNotFound() {
        Long appointmentId = 100L; //non-existing id

        MvcTestResult result = mvcTester.delete()
                .uri("/api/appointments/me/{id}", appointmentId)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyText().containsIgnoringCase("appointment");
    }

    @Test
    @WithMockCustomUser(id = 4, email = "maurycy@example.com")
    void deleteAppointmentShouldReturn403WhenAppointmentPatientIdIsNotEqualToRequestPatientId() {
        Long appointmentId = 1L; //not Maurycy's appointment

        MvcTestResult result = mvcTester.delete()
                .uri("/api/appointments/me/{id}", appointmentId)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(result).bodyText().containsIgnoringCase("denied");
    }

    @Test
    @WithMockCustomUser
    void deleteAppointmentShouldReturn400WhenAppointmentHasStatusFinished() {
        Long appointmentId = 1L;

        MvcTestResult result = mvcTester.delete()
                .uri("/api/appointments/me/{id}", appointmentId)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyText().containsIgnoringCase("finished");
    }

    @Test
    @WithMockCustomUser(id = 4, email = "maurycy@example.com")
    void deleteAppointmentShouldReturn200AndDeleteAppointmentWhenExistsForAppointmentIdAndPatientId(
            @Autowired ApplicationEvents applicationEvents
        ) {
        Long appointmentId = 5L;

        MvcTestResult result = mvcTester.delete()
                .uri("/api/appointments/me/{id}", appointmentId)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(appointmentRepository.findById(appointmentId)).isEmpty();
        assertThat(applicationEvents.stream(AppointmentDeletedEvent.class))
                .hasSize(1);
    }

}
