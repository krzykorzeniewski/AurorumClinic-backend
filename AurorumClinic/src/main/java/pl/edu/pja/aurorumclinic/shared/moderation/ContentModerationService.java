package pl.edu.pja.aurorumclinic.shared.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.ai.openai.OpenAiModerationOptions;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiConflictException;

@Service
@RequiredArgsConstructor
public class ContentModerationService {

    private final OpenAiModerationModel moderationModel;

    public void assertAllowed(String text, String fieldName) {
        if (text == null || text.isBlank()) return;

        OpenAiModerationOptions opts = OpenAiModerationOptions.builder()
                .model("omni-moderation-latest")
                .build();

        ModerationResponse response = moderationModel.call(new ModerationPrompt(text, opts));

        boolean flagged = response.getResult().getOutput()
                .getResults().stream().anyMatch(ModerationResult::isFlagged);

        if (flagged) {
            throw new ApiConflictException("Content rejected by moderation", fieldName);
        }
    }
}
