package pl.edu.pja.aurorumclinic.features.specialization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.specializations.commands.UpdateSpecialization;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {UpdateSpecialization.class})
@ActiveProfiles("test")
class UpdateSpecializationTest {

    @MockitoBean
    SpecializationRepository specializationRepository;

    @Autowired
    UpdateSpecialization controller;

    @Test
    void shouldThrowNotFoundWhenIdDoesNotExist() {
        Long id = 10L;
        UpdateSpecialization.UpdateSpecializationRequest req =
                new UpdateSpecialization.UpdateSpecializationRequest("Nowa nazwa");

        when(specializationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateSpecialization(id, req))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(specializationRepository).findById(id);
    }

    @Test
    void shouldUpdateNameWhenSpecializationExists() {
        Long id = 5L;

        Specialization spec = Specialization.builder()
                .id(id)
                .name("Stara nazwa")
                .build();

        UpdateSpecialization.UpdateSpecializationRequest req =
                new UpdateSpecialization.UpdateSpecializationRequest("Nowa nazwa");

        when(specializationRepository.findById(id)).thenReturn(Optional.of(spec));

        var resp = controller.updateSpecialization(id, req);

        verify(specializationRepository).findById(id);

        assertThat(spec.getName()).isEqualTo("Nowa nazwa");

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<?> body = resp.getBody();
        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).isNull();
    }
}