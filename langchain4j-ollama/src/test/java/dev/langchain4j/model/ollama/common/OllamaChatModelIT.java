package dev.langchain4j.model.ollama.common;

import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static dev.langchain4j.model.chat.common.ChatModelCapabilities.SupportStatus.DISABLED;
import static dev.langchain4j.model.chat.common.ChatModelCapabilities.SupportStatus.NOT_SUPPORTED;
import static dev.langchain4j.model.ollama.AbstractOllamaLanguageModelInfrastructure.OLLAMA_BASE_URL;
import static dev.langchain4j.model.ollama.AbstractOllamaLanguageModelInfrastructure.ollamaBaseUrl;
import static dev.langchain4j.model.ollama.OllamaImage.LLAMA_3_1;
import static dev.langchain4j.model.ollama.OllamaImage.LLAMA_3_2_VISION;
import static dev.langchain4j.model.ollama.OllamaImage.OLLAMA_IMAGE;
import static dev.langchain4j.model.ollama.OllamaImage.localOllamaImage;
import static dev.langchain4j.model.ollama.OllamaImage.resolve;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.common.AbstractChatModelIT;
import dev.langchain4j.model.chat.common.ChatLanguageModelCapabilities;
import dev.langchain4j.model.chat.common.ChatModelCapabilities;
import dev.langchain4j.model.ollama.LC4jOllamaContainer;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.util.List;

class OllamaChatModelIT extends AbstractChatModelIT {

    // TODO https://github.com/langchain4j/langchain4j/issues/2219

    private static final String MODEL_WITH_TOOLS = LLAMA_3_1;
    private static LC4jOllamaContainer ollamaWithTools;

    private static final String MODEL_WITH_VISION = LLAMA_3_2_VISION;
    private static LC4jOllamaContainer ollamaWithVision;

    static {
        if (isNullOrEmpty(OLLAMA_BASE_URL)) {
            String localOllamaImageWithTools = localOllamaImage(MODEL_WITH_TOOLS);
            ollamaWithTools = new LC4jOllamaContainer(resolve(OLLAMA_IMAGE, localOllamaImageWithTools))
                    .withModel(MODEL_WITH_TOOLS);
            ollamaWithTools.start();
            ollamaWithTools.commitToImage(localOllamaImageWithTools);

            String localOllamaImageWithVision = localOllamaImage(MODEL_WITH_VISION);
            ollamaWithVision = new LC4jOllamaContainer(resolve(OLLAMA_IMAGE, localOllamaImageWithVision))
                    .withModel(MODEL_WITH_VISION);
            ollamaWithVision.start();
            ollamaWithVision.commitToImage(localOllamaImageWithVision);
        }
    }

    static final ChatLanguageModelCapabilities OLLAMA_CHAT_MODEL_WITH_TOOLS = ChatLanguageModelCapabilities.builder()
            .model(OllamaChatModel.builder()
                    .baseUrl(ollamaBaseUrl(ollamaWithTools))
                    .modelName(MODEL_WITH_TOOLS)
                    .temperature(0.0)
                    .build())
            .mnemonicName("ollama_chat_model_with_tools")
            .supportsSingleImageInputAsPublicURL(DISABLED) // no exception thrown, image is just silently ignored
            .supportsSingleImageInputAsBase64EncodedString(
                    DISABLED) // no exception thrown, image is just silently ignored
            .supportsMaxOutputTokensParameter(NOT_SUPPORTED)
            .supportsModelNameParameter(NOT_SUPPORTED)
            .supportsStopSequencesParameter(NOT_SUPPORTED)
            .supportsToolChoiceRequired(NOT_SUPPORTED)
            .supportsCommonParametersWrappedInIntegrationSpecificClass(DISABLED) // to be implemented
            .supportsToolsAndJsonResponseFormatWithSchema(DISABLED)
            .assertExceptionType(false)
            .assertResponseId(false)
            .assertFinishReason(false)
            .assertResponseModel(false)
            .build();

    static final ChatLanguageModelCapabilities OLLAMA_CHAT_MODEL_WITH_VISION = ChatLanguageModelCapabilities.builder()
            .model(OllamaChatModel.builder()
                    .baseUrl(ollamaBaseUrl(ollamaWithVision))
                    .modelName(MODEL_WITH_VISION)
                    .temperature(0.0)
                    .build())
            .mnemonicName("ollama_chat_model_with_vision")
            .supportsMaxOutputTokensParameter(NOT_SUPPORTED)
            .supportsModelNameParameter(NOT_SUPPORTED)
            .supportsTools(NOT_SUPPORTED)
            .supportsMultipleImageInputsAsBase64EncodedStrings(NOT_SUPPORTED)
            .supportsMultipleImageInputsAsPublicURLs(NOT_SUPPORTED)
            .supportsStopSequencesParameter(NOT_SUPPORTED)
            .supportsCommonParametersWrappedInIntegrationSpecificClass(DISABLED) // to be implemented
            .assertExceptionType(false)
            .assertResponseId(false)
            .assertFinishReason(false)
            .assertResponseModel(false)
            .build();

    static final ChatLanguageModelCapabilities OPEN_AI_CHAT_MODEL_WITH_TOOLS = ChatLanguageModelCapabilities.builder()
            .model(OpenAiChatModel.builder()
                    .apiKey("does not matter")
                    .baseUrl(ollamaBaseUrl(ollamaWithTools) + "/v1")
                    .modelName(MODEL_WITH_TOOLS)
                    .temperature(0.0)
                    .build())
            .mnemonicName("open_ai_chat_model_with_tools")
            .supportsSingleImageInputAsPublicURL(NOT_SUPPORTED)
            .supportsSingleImageInputAsBase64EncodedString(
                    DISABLED) // no exception thrown, image is just silently ignored
            .supportsModelNameParameter(NOT_SUPPORTED)
            .supportsCommonParametersWrappedInIntegrationSpecificClass(DISABLED) // to be implemented
            .supportsToolsAndJsonResponseFormatWithSchema(DISABLED)
            .assertExceptionType(false)
            .assertResponseId(false)
            .assertFinishReason(false)
            .assertResponseModel(false)
            .build();

    static final ChatLanguageModelCapabilities OPEN_AI_CHAT_MODEL_WITH_VISION = ChatLanguageModelCapabilities.builder()
            .model(OpenAiChatModel.builder()
                    .apiKey("does not matter")
                    .baseUrl(ollamaBaseUrl(ollamaWithVision) + "/v1")
                    .modelName(MODEL_WITH_VISION)
                    .temperature(0.0)
                    .build())
            .mnemonicName("open_ai_chat_model_with_vision")
            .supportsModelNameParameter(NOT_SUPPORTED)
            .supportsTools(NOT_SUPPORTED)
            .supportsMultipleImageInputsAsBase64EncodedStrings(NOT_SUPPORTED)
            .supportsSingleImageInputAsPublicURL(NOT_SUPPORTED) // getting invalid image input from model
            .supportsCommonParametersWrappedInIntegrationSpecificClass(DISABLED) // to be implemented
            .assertExceptionType(false)
            .assertResponseId(false)
            .assertFinishReason(false)
            .assertResponseModel(false)
            .build();

    @Override
    protected List<ChatModelCapabilities<ChatLanguageModel>> models() {
        return List.of(
                OLLAMA_CHAT_MODEL_WITH_TOOLS,
                OPEN_AI_CHAT_MODEL_WITH_TOOLS,
                OLLAMA_CHAT_MODEL_WITH_VISION,
                OPEN_AI_CHAT_MODEL_WITH_VISION
                // TODO add more model configs, see OpenAiChatModelIT
                );
    }

    @Override
    protected boolean supportsModelNameParameter() {
        return false; // TODO implement
    }

    @Override
    protected boolean supportsMaxOutputTokensParameter() {
        return false; // TODO implement
    }

    @Override
    protected boolean supportsStopSequencesParameter() {
        return false; // TODO implement
    }
}
