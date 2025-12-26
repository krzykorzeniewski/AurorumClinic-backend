package pl.edu.pja.aurorumclinic.features.specialization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.specializations.queries.GetSpecializationById;
import pl.edu.pja.aurorumclinic.features.specializations.queries.shared.GetSpecializationResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {GetSpecializationById.class})
@ActiveProfiles("test")
class GetSpecializationByIdTest {

    @MockitoBean
    SpecializationRepository specializationRepository;

    @Autowired
    GetSpecializationById controller;

    @Test
    void shouldReturnSpecializationWhenFound() {
        Long specId = 10L;

        GetSpecializationResponse dto = new GetSpecializationResponse(
                specId,
                "Psychiatria"
        );

        when(specializationRepository.findSpecializationDtoById(specId))
                .thenReturn(Optional.of(dto));

        var resp = controller.getSpecializationById(specId);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<GetSpecializationResponse> body = resp.getBody();

        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).isEqualTo(dto);

        verify(specializationRepository).findSpecializationDtoById(specId);
    }

    @Test
    void shouldThrowNotFoundWhenIdDoesNotExist() {
        Long specId = 99L;

        when(specializationRepository.findSpecializationDtoById(specId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getSpecializationById(specId))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(specializationRepository).findSpecializationDtoById(specId);
    }
}