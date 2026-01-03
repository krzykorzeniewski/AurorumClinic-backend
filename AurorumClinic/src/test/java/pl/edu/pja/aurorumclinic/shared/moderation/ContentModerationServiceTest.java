package pl.edu.pja.aurorumclinic.shared.moderation;


import org.junit.jupiter.api.Test;
import org.springframework.ai.moderation.*;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {ContentModerationService.class})
@ActiveProfiles("test")
class ContentModerationServiceTest {

    @MockitoBean
    OpenAiModerationModel moderationModel;

    @Autowired
    ContentModerationService service;

    @Test
    void shouldNotCallModelWhenTextIsNullOrBlank() {
        assertThatCode(() -> service.assertAllowed(null, "comment"))
                .doesNotThrowAnyException();

        assertThatCode(() -> service.assertAllowed("   ", "comment"))
                .doesNotThrowAnyException();

        verifyNoInteractions(moderationModel);
    }

    @Test
    void shouldPassWhenContentValidAndNotFlagged() {

        CategoryScores categoryScores = mock(CategoryScores.class);

        ModerationResult result = mock(ModerationResult.class);
        when(result.getCategoryScores()).thenReturn(categoryScores);
        when(result.isFlagged()).thenReturn(false);

        Moderation moderation = mock(Moderation.class);
        when(moderation.getResults()).thenReturn(List.of(result));

        Generation generation = mock(Generation.class);
        when(generation.getOutput()).thenReturn(moderation);

        ModerationResponse response = mock(ModerationResponse.class);
        when(response.getResult()).thenReturn(generation);

        when(moderationModel.call(any(ModerationPrompt.class))).thenReturn(response);

        assertThatCode(() -> service.assertAllowed("normalny tekst", "comment"))
                .doesNotThrowAnyException();

        verify(moderationModel).call(any(ModerationPrompt.class));
    }

    @Test
    void shouldThrowApiExceptionWhenContentFlagged() {

        CategoryScores categoryScores = mock(CategoryScores.class);
        when(categoryScores.getSexual()).thenReturn(0.8);

        ModerationResult result = mock(ModerationResult.class);
        when(result.isFlagged()).thenReturn(true);
        when(result.getCategoryScores()).thenReturn(categoryScores);

        Moderation moderation = mock(Moderation.class);
        when(moderation.getResults()).thenReturn(List.of(result));

        Generation generation = mock(Generation.class);
        when(generation.getOutput()).thenReturn(moderation);

        ModerationResponse response = mock(ModerationResponse.class);
        when(response.getResult()).thenReturn(generation);

        when(moderationModel.call(any(ModerationPrompt.class))).thenReturn(response);

        assertThatThrownBy(() -> service.assertAllowed("toxic tekst", "comment"))
                .isExactlyInstanceOf(ApiException.class)
                .hasMessageContaining("Content rejected by moderation");

        verify(moderationModel).call(any(ModerationPrompt.class));
    }
}