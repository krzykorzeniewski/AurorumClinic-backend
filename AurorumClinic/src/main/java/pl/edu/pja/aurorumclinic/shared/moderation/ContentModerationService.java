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
        if (categoryScores.getSexual() > 0.2) {
            throw new ApiException("Content rejected by moderation", "sexual");
        }
        if (categoryScores.getHate() > 0.2) {
            throw new ApiException("Content rejected by moderation", "hate");
        }
        if (categoryScores.getHarassment() > 0.2) {
            throw new ApiException("Content rejected by moderation", "harassment");
        }
        if (categoryScores.getSelfHarm() > 0.2) {
            throw new ApiException("Content rejected by moderation", "self-harm");
        }
        if (categoryScores.getSexualMinors() > 0.2) {
            throw new ApiException("Content rejected by moderation", "sexual/minors");
        }
        if (categoryScores.getHateThreatening() > 0.2) {
            throw new ApiException("Content rejected by moderation", "hate/threatening");
        }
        if (categoryScores.getViolenceGraphic() > 0.2) {
            throw new ApiException("Content rejected by moderation", "violence/graphic");
        }
        if (categoryScores.getSelfHarmIntent() > 0.2) {
            throw new ApiException("Content rejected by moderation", "self-harm/intent");
        }
        if (categoryScores.getSelfHarmInstructions() > 0.2) {
            throw new ApiException("Content rejected by moderation", "self-harm/instructions");
        }
        if (categoryScores.getHarassmentThreatening() > 0.2) {
            throw new ApiException("Content rejected by moderation", "harassment/threatening");
        }
        if (categoryScores.getViolence() > 0.2) {
            throw new ApiException("Content rejected by moderation", "violence");
        }
        if (categoryScores.getDangerousAndCriminalContent() > 0.2) {
            throw new ApiException("Content rejected by moderation", "dangerous-and-criminal-content");
        }
        if (categoryScores.getHealth() > 0.2) {
            throw new ApiException("Content rejected by moderation", "health");
        }
        if (categoryScores.getFinancial() > 0.2) {
            throw new ApiException("Content rejected by moderation", "financial");
        }
        if (categoryScores.getLaw() > 0.2) {
            throw new ApiException("Content rejected by moderation", "law");
        }
        if (categoryScores.getPii() > 0.2) {
            throw new ApiException("Content rejected by moderation", "pii");
        }
    }
}
