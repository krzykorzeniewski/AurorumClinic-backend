package pl.edu.pja.aurorumclinic.features.specialization;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.specializations.commands.CreateSpecialization;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {CreateSpecialization.class})
@ActiveProfiles("test")
class CreateSpecializationTest {

    @MockitoBean
    SpecializationRepository specializationRepository;

    @Autowired
    CreateSpecialization controller;

    @Test
    void shouldCreateSpecializationAndSaveToRepository() {
        CreateSpecialization.CreateSpecializationRequest req =
                new CreateSpecialization.CreateSpecializationRequest("Psychiatria");

        var resp = controller.createSpecialization(req);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<?> body = resp.getBody();
        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).isNull();

        ArgumentCaptor<Specialization> captor = ArgumentCaptor.forClass(Specialization.class);
        verify(specializationRepository).save(captor.capture());

        Specialization saved = captor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Psychiatria");
    }
}