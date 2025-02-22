package dev.langchain4j.model.googleai.common;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.common.AbstractChatModelIT;
import dev.langchain4j.model.chat.common.ChatLanguageModelCapabilities;
import dev.langchain4j.model.chat.common.ChatModelCapabilities;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import java.util.List;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.model.chat.common.ChatModelCapabilities.SupportStatus.NOT_SUPPORTED;

class GoogleAiGeminiChatModelIT extends AbstractChatModelIT {
// TODO add streaming counterpart

    // TODO https://github.com/langchain4j/langchain4j/issues/2219
    // TODO https://github.com/langchain4j/langchain4j/issues/2220

    static final GoogleAiGeminiChatModel GOOGLE_AI_GEMINI_CHAT_MODEL = GoogleAiGeminiChatModel.builder()
            .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
            .modelName("gemini-1.5-flash-8b")
            .build();

    @Override
    protected List<ChatModelCapabilities<ChatLanguageModel>> models() {
        return List.of(
                ChatLanguageModelCapabilities.builder()
                        .model(GOOGLE_AI_GEMINI_CHAT_MODEL)
                        .mnemonicName("google ai gemini chat model")
                        .supportsSingleImageInputAsPublicURL(NOT_SUPPORTED) // TODO check if supported
                        .supportsToolChoiceRequired(NOT_SUPPORTED) // TODO implement
                        .supportsToolsAndJsonResponseFormatWithSchema(NOT_SUPPORTED) // TODO fix
                        .assertExceptionType(false) // TODO fix
                        .assertResponseId(false) // TODO implement
                        .assertFinishReason(false) // TODO implement
                        .assertResponseModel(false) // TODO implement
                        .build());
        // TODO add more model configs, see OpenAiChatModelIT
    }

    @Override
    protected String customModelName() {
        return "gemini-1.5-flash";
    }

    @Override
    protected ChatLanguageModel createModelWith(ChatRequestParameters parameters) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))

                // TODO re-implement, support .defaultRequestParameters(ChatRequestParameters)
                .modelName(getOrDefault(parameters.modelName(), "gemini-1.5-flash-8b"))
                .temperature(parameters.temperature())
                .topP(parameters.topP())
                .topK(parameters.topK())
                .maxOutputTokens(parameters.maxOutputTokens())
                .stopSequences(parameters.stopSequences())
                .responseFormat(parameters.responseFormat())

                .build();
    }

    @Override
    protected ChatRequestParameters createIntegrationSpecificParameters(int maxOutputTokens) {
        return ChatRequestParameters.builder() // TODO return Gemini-specific params
                .maxOutputTokens(maxOutputTokens)
                .build();
    }

    @Override
    protected boolean supportsToolChoiceRequired() {
        return false; // TODO implement
    }

    @Override
    protected boolean assertResponseId() {
        return false; // TODO implement
    }

    @Override
    protected boolean assertResponseModel() {
        return false; // TODO implement
    }

    protected boolean assertFinishReason() {
        return false; // TODO implement
    }

    @Override
    protected boolean assertExceptionType() {
        return false; // TODO fix
    }
}
