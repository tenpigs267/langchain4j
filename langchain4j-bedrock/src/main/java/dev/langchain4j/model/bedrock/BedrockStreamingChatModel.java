package dev.langchain4j.model.bedrock;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.model.chat.request.ToolChoice.REQUIRED;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.listener.ListenersUtil;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlockDelta;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseStreamResponseHandler;

/**
 * BedrockStreamingChatModel uses the Bedrock ConverseAPI.
 *
 * @see <a href="https://docs.aws.amazon.com/bedrock/latest/userguide/conversation-inference.html">https://docs.aws.amazon.com/bedrock/latest/userguide/conversation-inference.html</a>
 */
public class BedrockStreamingChatModel extends AbstractBedrockChatModel implements StreamingChatLanguageModel {
    private final BedrockRuntimeAsyncClient client;

    public BedrockStreamingChatModel(String modelId) {
        this(builder().modelId(modelId));
    }

    private BedrockStreamingChatModel(Builder builder) {
        super(builder);
        this.client = isNull(builder.client)
                ? createClient(
                        getOrDefault(builder.getLogRequests(), false), getOrDefault(builder.getLogResponses(), false))
                : builder.client;
    }

    @Override
    public void chat(final ChatRequest chatRequest, final StreamingChatResponseHandler handler) {
        Map<Object, Object> attributes = new ConcurrentHashMap<>();
        final ConverseStreamRequest converseStreamRequest = buildConverseStreamRequest(
                chatRequest.messages(), chatRequest.parameters().toolSpecifications(), chatRequest.parameters());
        ChatRequest finalChatRequest = ChatRequest.builder()
                .messages(chatRequest.messages())
                .parameters(this.defaultRequestParameters.overrideWith(chatRequest.parameters()))
                .build();

        ConverseResponseFromStreamBuilder converseResponseBuilder = ConverseResponseFromStreamBuilder.builder();
        final ConverseStreamResponseHandler built = ConverseStreamResponseHandler.builder()
                .subscriber(ConverseStreamResponseHandler.Visitor.builder()
                        .onContentBlockStart(converseResponseBuilder::append)
                        .onContentBlockDelta(chunk -> {
                            if (chunk.delta().type().equals(ContentBlockDelta.Type.TEXT)) {
                                handler.onPartialResponse(chunk.delta().text());
                            }
                            converseResponseBuilder.append(chunk);
                        })
                        .onContentBlockStop(converseResponseBuilder::append)
                        .onMetadata(chunk -> {
                            converseResponseBuilder.append(chunk);
                            final ChatResponse completeResponse =
                                    chatResponseFrom(converseResponseBuilder.build(), converseStreamRequest.modelId());
                            ListenersUtil.onResponse(completeResponse, finalChatRequest, attributes, listeners);
                            handler.onCompleteResponse(completeResponse);
                        })
                        .onMessageStart(converseResponseBuilder::append)
                        .onMessageStop(converseResponseBuilder::append)
                        .build())
                .onError(error -> {
                    handler.onError(error);
                    ListenersUtil.onError(error, finalChatRequest, attributes, listeners);
                })
                .build();

        ListenersUtil.onRequest(finalChatRequest, attributes, listeners);
        this.client.converseStream(converseStreamRequest, built).join();
    }

    public static Response<AiMessage> convertResponse(ChatResponse chatResponse) {
        return Response.from(
                chatResponse.aiMessage(),
                chatResponse.metadata().tokenUsage(),
                chatResponse.metadata().finishReason());
    }

    static StreamingChatResponseHandler convertHandler(StreamingResponseHandler<AiMessage> handler) {
        return new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                handler.onNext(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                handler.onComplete(convertResponse(completeResponse));
            }

            @Override
            public void onError(Throwable error) {
                handler.onError(error);
            }
        };
    }

    @Override
    public void generate(List<ChatMessage> messages, StreamingResponseHandler<AiMessage> handler) {
        ChatRequest chatRequest = ChatRequest.builder().messages(messages).build();
        chat(chatRequest, convertHandler(handler));
    }

    @Override
    public void generate(
            List<ChatMessage> messages,
            List<ToolSpecification> toolSpecifications,
            StreamingResponseHandler<AiMessage> handler) {
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                .parameters(ChatRequestParameters.builder()
                        .toolSpecifications(toolSpecifications)
                        .build())
                .build();
        chat(chatRequest, convertHandler(handler));
    }

    @Override
    public void generate(
            List<ChatMessage> messages,
            ToolSpecification toolSpecification,
            StreamingResponseHandler<AiMessage> handler) {
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                .parameters(ChatRequestParameters.builder()
                        .toolSpecifications(toolSpecification)
                        .toolChoice(REQUIRED)
                        .build())
                .build();
        chat(chatRequest, convertHandler(handler));
    }

    private ConverseStreamRequest buildConverseStreamRequest(
            List<ChatMessage> messages, List<ToolSpecification> toolSpecs, ChatRequestParameters parameters) {
        final String model =
                isNull(parameters) || isNull(parameters.modelName()) ? this.modelId : parameters.modelName();

        if (nonNull(parameters)) validate(parameters);

        return ConverseStreamRequest.builder()
                .modelId(model)
                .inferenceConfig(inferenceConfigurationFrom(parameters))
                .system(extractSystemMessages(messages))
                .messages(extractRegularMessages(messages))
                .toolConfig(extractToolConfigurationFrom(toolSpecs, parameters))
                .additionalModelRequestFields(additionalRequestModelFieldsFrom(parameters))
                .build();
    }

    private ChatResponse chatResponseFrom(ConverseResponse converseResponse, String modelId) {
        return ChatResponse.builder()
                .aiMessage(aiMessageFrom(converseResponse))
                .metadata(ChatResponseMetadata.builder()
                        .id(UUID.randomUUID().toString())
                        .finishReason(finishReasonFrom(converseResponse.stopReason()))
                        .tokenUsage(tokenUsageFrom(converseResponse.usage()))
                        .modelName(modelId)
                        .build())
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private BedrockRuntimeAsyncClient createClient(boolean logRequests, boolean logResponses) {
        return BedrockRuntimeAsyncClient.builder()
                .region(this.region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .overrideConfiguration(config -> {
                    config.apiCallTimeout(this.timeout);
                    if (logRequests || logResponses)
                        config.addExecutionInterceptor(new AwsLoggingInterceptor(logRequests, logResponses));
                })
                .build();
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private BedrockRuntimeAsyncClient client;

        public Builder client(BedrockRuntimeAsyncClient client) {
            this.client = client;
            return this;
        }

        public BedrockStreamingChatModel build() {
            return new BedrockStreamingChatModel(this);
        }
    }
}
