package dev.langchain4j.model.bedrock;

import static dev.langchain4j.model.bedrock.TestedModelsWithConverseAPI.AWS_NOVA_LITE;
import static dev.langchain4j.model.bedrock.TestedModelsWithConverseAPI.AWS_NOVA_MICRO;
import static dev.langchain4j.model.bedrock.TestedModelsWithConverseAPI.AWS_NOVA_PRO;
import static dev.langchain4j.model.bedrock.TestedModelsWithConverseAPI.CLAUDE_3_HAIKU;
import static dev.langchain4j.model.bedrock.TestedModelsWithConverseAPI.COHERE_COMMAND_R_PLUS;
import static dev.langchain4j.model.bedrock.TestedModelsWithConverseAPI.MISTRAL_LARGE;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.common.AbstractAiServiceIT;
import java.util.List;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "AWS_SECRET_ACCESS_KEY", matches = ".+")
public class BedrockAiServicesIT extends AbstractAiServiceIT {

    @Override
    protected List<ChatLanguageModel> models() {
        return List.of(
                AWS_NOVA_MICRO.model(),
                AWS_NOVA_LITE.model(),
                AWS_NOVA_PRO.model(),
                COHERE_COMMAND_R_PLUS.model(),
                MISTRAL_LARGE.model(),
                CLAUDE_3_HAIKU.model());
    }
}
