package pl.edu.pja.aurorumclinic.shared.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.moderation.CategoryScores;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.ai.openai.OpenAiModerationOptions;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

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

        response.getResult().getOutput().getResults()
                .forEach(moderationResult ->
                        checkModerationResults(moderationResult.getCategoryScores()));
    }

    private void checkModerationResults(CategoryScores categoryScores) {
        if (categoryScores.getSexual() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getHate() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getHarassment() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getSelfHarm() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getSexualMinors() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getHateThreatening() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getViolenceGraphic() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getSelfHarmIntent() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getSelfHarmInstructions() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getHarassmentThreatening() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getViolence() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getDangerousAndCriminalContent() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getHealth() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getFinancial() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getLaw() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
        if (categoryScores.getPii() > 0.09) {
            throw new ApiException("Content rejected by moderation", "opinionContent");
        }
    }
}
