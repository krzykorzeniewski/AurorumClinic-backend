package pl.edu.pja.aurorumclinic.features.opinions.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {AdminDeleteOpinion.class})
@ActiveProfiles("test")
class AdminDeleteOpinionTest {

    @MockitoBean
    OpinionRepository opinionRepository;

    @Autowired
    AdminDeleteOpinion controller;

    @Test
    void shouldDeleteOpinionWhenExists() {
        Long opinionId = 1L;

        Opinion op = mock(Opinion.class);
        when(opinionRepository.findById(opinionId)).thenReturn(Optional.of(op));

        controller.delete(opinionId);

        verify(opinionRepository).findById(opinionId);
        verify(opinionRepository).delete(op);
        verifyNoMoreInteractions(opinionRepository);
    }

    @Test
    void shouldThrowNotFoundWhenOpinionMissing() {
        Long opinionId = 999L;

        when(opinionRepository.findById(opinionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.delete(opinionId))
                .isInstanceOf(ApiNotFoundException.class);

        verify(opinionRepository).findById(opinionId);
        verify(opinionRepository, never()).delete(any());
        verifyNoMoreInteractions(opinionRepository);
    }
}
