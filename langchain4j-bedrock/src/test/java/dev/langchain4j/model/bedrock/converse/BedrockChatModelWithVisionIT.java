package dev.langchain4j.model.bedrock.converse;

import static dev.langchain4j.model.bedrock.converse.BedrockChatModel.dblToFloat;
import static dev.langchain4j.model.bedrock.converse.TestedModels.*;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.common.AbstractChatModelIT;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import java.util.List;

public class BedrockChatModelWithVisionIT extends AbstractChatModelIT {
    @Override
    protected List<ChatLanguageModel> models() {
        return List.of(AWS_NOVA_LITE);
    }

    @Override
    protected String customModelName() {
        return "anthropic.claude-3-5-sonnet-20240620-v1:0";
    }

    @Override
    protected ChatRequestParameters createIntegrationSpecificParameters(int maxOutputTokens) {
        return ChatRequestParameters.builder().maxOutputTokens(maxOutputTokens).build();
    }

    @Override
    protected ChatLanguageModel createModelWith(ChatRequestParameters parameters) {
        // TODO
        return BedrockChatModel.builder()
                .modelId(parameters.modelName())
                .stopSequences(parameters.stopSequences())
                .temperature(dblToFloat(parameters.temperature()))
                .topP(dblToFloat(parameters.topP()))
                .maxTokens(parameters.maxOutputTokens())
                .build();
    }

    @Override
    protected boolean supportsDefaultRequestParameters() {
        return false;
    }

    @Override
    protected boolean supportsModelNameParameter() {
        return true;
    }

    @Override
    protected boolean supportsMaxOutputTokensParameter() {
        return true;
    }

    @Override
    protected boolean supportsStopSequencesParameter() {
        return true;
    }

    @Override
    protected boolean supportsTools() {
        return true;
    }

    @Override
    protected boolean supportsToolChoiceRequired() {
        return false;
    }

    @Override
    protected boolean supportsToolChoiceRequiredWithSingleTool() {
        return supportsToolChoiceRequired();
    }

    @Override
    protected boolean supportsToolChoiceRequiredWithMultipleTools() {
        return supportsToolChoiceRequired();
    }

    @Override
    protected boolean supportsJsonResponseFormat() {
        return false;
    }

    @Override
    protected boolean supportsJsonResponseFormatWithSchema() {
        return false;
    }

    @Override
    protected boolean supportsToolsAndJsonResponseFormatWithSchema() {
        return supportsTools() && supportsJsonResponseFormatWithSchema();
    }

    @Override
    protected boolean supportsSingleImageInputAsBase64EncodedString() {
        return true;
    }

    @Override
    protected boolean supportsMultipleImageInputsAsBase64EncodedStrings() {
        return supportsSingleImageInputAsBase64EncodedString();
    }

    @Override
    protected boolean supportsSingleImageInputAsPublicURL() {
        return true;
    }

    @Override
    protected boolean supportsMultipleImageInputsAsPublicURLs() {
        return supportsSingleImageInputAsPublicURL();
    }
}
