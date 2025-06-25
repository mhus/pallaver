package de.mhus.pallaver.model;

import de.mhus.commons.tools.MString;
import de.mhus.pallaver.chat.ChatAssistant;
import de.mhus.pallaver.chat.ChatOptions;
import de.mhus.pallaver.chat.StreamChatAssistant;
import de.mhus.pallaver.lltype.LLMFeatures;
import de.mhus.pallaver.ui.Bubble;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class SingleModelControl implements ModelControl {

    @Getter
    LLModel model;
    private final ModelService modelService;
    @Getter
    volatile boolean enabled = false;
    @Getter
    private StreamingChatModel streamChatModel;
    @Getter
    private ChatModel chatModel;
    @Getter
    private ChatMemory chatMemory;
    @Setter
    @Getter
    private ChatOptions chatOptions = new ChatOptions();
    private StreamChatAssistant streamChatAssistant;
    private ChatAssistant chatAssistant;
    @Getter
    private Exception exception;
    @Getter
    private TokenCountEstimator tokenizer;

    public SingleModelControl(LLModel model, ModelService modelService, ChatOptions chatOptions) {
        this.model = model;
        this.modelService = modelService;
        this.chatOptions = chatOptions;
    }

    public String getTitle() {
        return model.getTitle();
    }

    public AiMessage answer(String userMessage) {
        return answer(getTitle(), userMessage);
    }

    public AiMessage answer(String title, String userMessage) {
        Bubble otherBubble = null;
        exception = null;
        try {

            initModel();
            otherBubble = addChatBubble(title);

            addToChatMemory(userMessage);
            CompletableFuture<AiMessage> futureAiMessage = new CompletableFuture<>();
            StreamingChatResponseHandler handler = createChatMessageHandler(futureAiMessage, otherBubble);

            if (chatOptions.isUseTools() && modelService.supports(model, LLMFeatures.TOOLS)) {
                if (isStreamChatModel()) {
                    answerWithStreamChatAssistant(userMessage, handler);
                } else {
                    answerWithChatAssistant(userMessage, handler);
                }
            } else {
                if (isStreamChatModel()) {
                    answerWithStreamChatModel(userMessage, handler);
                } else {
                    answerWithChatModel(userMessage, handler);
                }
            }
            var answer = futureAiMessage.get();
            chatMemory.add(answer);
            return answer;
        } catch (Exception e) {
            LOGGER.error("Error", e);
            if (otherBubble != null)
                otherBubble = addChatBubble(model.getTitle());
            otherBubble.appendText("Error: " + e.getMessage());
            exception = e;
            return AiMessage.from("Error: " + e.getMessage());
        }
    }

    public boolean isStreamChatModel() {
        return  chatOptions.getMode() == ChatOptions.MODE.STREAM // force streaming by config
                ||
                chatOptions.getMode() == ChatOptions.MODE.AUTO
                &&
                modelService.supports(model, LLMFeatures.STREAM) // if stream is supported and not tools
                &&
                (!chatOptions.isUseTools() || modelService.supports(model, LLMFeatures.STREAM_TOOLS)); // if tooling and stream tool is supported
    }

    public void answerWithChatModel(String userMessage, StreamingChatResponseHandler handler) {
        var answer = chatModel.chat(chatMemory.messages());
        handler.onPartialResponse(answer.aiMessage().text());
        handler.onCompleteResponse(answer);
    }

    public void answerWithStreamChatModel(String userMessage, StreamingChatResponseHandler handler) {
        streamChatModel.chat(chatMemory.messages(), handler);
    }

    public void answerWithChatAssistant(String userMessage, StreamingChatResponseHandler handler) {
        String answer = chatAssistant.generate(userMessage);
        handler.onPartialResponse(answer);
        handler.onCompleteResponse(ChatResponse.builder().aiMessage(AiMessage.from(answer)).build());
    }

    public void answerWithStreamChatAssistant(String userMessage, StreamingChatResponseHandler handler) {
        TokenStream tokenStream = streamChatAssistant.generate(chatMemory.messages());
        tokenStream.onPartialResponse(handler::onPartialResponse);
        tokenStream.onCompleteResponse(handler::onCompleteResponse);
        tokenStream.onError(handler::onError);
        tokenStream.start();
    }

    private StreamingChatResponseHandler createChatMessageHandler(CompletableFuture<AiMessage> futureAiMessage, Bubble otherBubble) {
        return new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String token) {
                otherBubble.appendText(token);
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                futureAiMessage.complete(response.aiMessage());
                otherBubble.onComplete();
            }

            @Override
            public void onError(Throwable e) {
                LOGGER.error("Error", e);
                otherBubble.appendText("Error: " + e.getMessage());
                otherBubble.onComplete();
            }
        };

    }

    protected void addToChatMemory(String userMessage) {
        chatMemory.add(UserMessage.userMessage(userMessage));
    }

    public void initModel() {

        if (chatMemory != null || streamChatModel != null) return;

        chatMemory = createChatMemory();
        if (MString.isSet(chatOptions.getPrompt())) {
            chatMemory.add(SystemMessage.from(chatOptions.getPrompt()));
        }

        if (isStreamChatModel()) {
            LOGGER.info("Use stream chat model: {} and tooling {}", model.getTitle(),chatOptions.isUseTools());
            if (streamChatModel == null) {
                streamChatModel = createStreamChatModel();
                streamChatAssistant = createStreamChatAssistant();

            }
        } else {
            LOGGER.info("Use chat model: {} and tooling {}", model.getTitle(),chatOptions.isUseTools());
            if (chatModel == null) {
                chatModel = createChatModel();
                chatAssistant = createChatAssistant();
            }
        }
    }

    public ChatModel createChatModel() {
        return modelService.createChatModel(model, chatOptions.getModelOptions());
    }

    public StreamingChatModel createStreamChatModel() {
        return modelService.createStreamingChatModel(model, chatOptions.getModelOptions());
    }

    public ChatMemory createChatMemory() {
        tokenizer = modelService.createTokenizer(model);
        return TokenWindowChatMemory.withMaxTokens(modelService.getMaxTokens(model, chatOptions), tokenizer);
    }

    public ChatAssistant createChatAssistant() {
        return AiServices.builder(ChatAssistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(chatOptions.getMaxMessages()))
                .tools(createTools())
                .build();
    }

    public StreamChatAssistant createStreamChatAssistant() {
        return AiServices.builder(StreamChatAssistant.class)
                .streamingChatModel(streamChatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(chatOptions.getMaxMessages()))
                .tools(createTools())
                .build();
    }

    public void reset(ChatOptions options) {
        if (options == null) options = chatOptions;
        if (chatMemory != null)
            chatMemory.clear();
        streamChatModel = null;
        chatModel = null;
        chatMemory = null;
        streamChatAssistant = null;
        chatAssistant = null;
        this.chatOptions = options;
    }

    protected abstract Bubble addChatBubble(String title);

    protected List<Object> createTools() {
        return List.of();
    }

}