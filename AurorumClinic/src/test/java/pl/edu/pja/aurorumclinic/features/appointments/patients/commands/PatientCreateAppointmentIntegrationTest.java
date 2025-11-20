package pl.edu.pja.aurorumclinic.features.appointments.patients.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import pl.edu.pja.aurorumclinic.IntegrationTest;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PatientCreateAppointmentIntegrationTest extends IntegrationTest {

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void name() {

    }
}
