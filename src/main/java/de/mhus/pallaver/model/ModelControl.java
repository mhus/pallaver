package de.mhus.pallaver.model;

import de.mhus.commons.tools.MString;
import de.mhus.pallaver.chat.ChatAssistant;
import de.mhus.pallaver.lltype.XChatModel;
import de.mhus.pallaver.ui.Bubble;
import de.mhus.pallaver.chat.ChatOptions;
import de.mhus.pallaver.lltype.LLMFeatures;
import de.mhus.pallaver.chat.StreamChatAssistant;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class ModelControl {

    @Getter
    LLModel model;
    private final ModelService modelService;
    @Getter
    volatile boolean enabled = false;
    @Getter
    private XChatModel chatModel;
    @Getter
    private ChatMemory chatMemory;
    @Setter
    @Getter
    private ChatOptions chatOptions = new ChatOptions();
    private ChatAssistant chatAssistant;
    @Getter
    private Exception exception;
    @Getter
    private TokenCountEstimator tokenizer;

    public ModelControl(LLModel model, ModelService modelService, ChatOptions chatOptions) {
        this.model = model;
        this.modelService = modelService;
        this.chatOptions = chatOptions;
    }

    public String getTitle() {
        return model.getTitle();
    }

    public AiMessage answer(String userMessage) {
        Bubble otherBubble = null;
        exception = null;
        try {

            initModel();
            otherBubble = addChatBubble(model.getTitle());

            addToChatMemory(userMessage);
            CompletableFuture<AiMessage> futureAiMessage = new CompletableFuture<>();
            StreamingResponseHandler<AiMessage> handler = createChatMessageHandler(futureAiMessage, otherBubble);

            if (chatOptions.isUseTools() && modelService.supports(model, LLMFeatures.TOOLS)) {
                answerWithChatAssistant(userMessage, handler);
            } else {
                answerWithChatModel(userMessage, handler);
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

    public void answerWithChatModel(String userMessage, StreamingResponseHandler<AiMessage> handler) {
        var answer = chatModel.chat(chatMemory.messages());
        handler.onNext(answer.aiMessage().text());
        handler.onComplete(Response.from(answer.aiMessage()));
    }

    public void answerWithChatAssistant(String userMessage, StreamingResponseHandler<AiMessage> handler) {
        String answer = chatAssistant.generate(userMessage);
        handler.onNext(answer);
        handler.onComplete(Response.from(AiMessage.from(answer)));
    }

    private StreamingResponseHandler<AiMessage> createChatMessageHandler(CompletableFuture<AiMessage> futureAiMessage, Bubble otherBubble) {
        return new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                otherBubble.appendText(token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                futureAiMessage.complete(response.content());
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

    private void addToChatMemory(String userMessage) {
        chatMemory.add(UserMessage.userMessage(userMessage));
    }

    public void initModel() {

        if (chatMemory != null) return;

        chatMemory = createChatMemory();
        if (MString.isSet(chatOptions.getPrompt())) {
            chatMemory.add(SystemMessage.from(chatOptions.getPrompt()));
        }
        LOGGER.info("Use chat model: {} and tooling {}", model.getTitle(),chatOptions.isUseTools());
        if (chatModel == null) {
            chatModel = createChatModel();
            chatAssistant = createChatAssistant();
        }
    }

    public XChatModel createChatModel() {
        return modelService.createChatModel(model, chatOptions.getModelOptions(), isStreamChatModel());
    }

    public ChatMemory createChatMemory() {
        tokenizer = modelService.createTokenizer(model);
        return TokenWindowChatMemory.withMaxTokens(chatOptions.getMaxTokens(), tokenizer);
    }

    public ChatAssistant createChatAssistant() {
        return AiServices.builder(ChatAssistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(chatOptions.getMaxMessages()))
                .tools(createTools())
                .build();
    }

    public void reset(ChatOptions options) {
        if (options == null) options = chatOptions;
        if (chatMemory != null)
            chatMemory.clear();
        chatModel = null;
        chatMemory = null;
        chatAssistant = null;
        this.chatOptions = options;
    }

    protected abstract Bubble addChatBubble(String title);

    protected List<Object> createTools() {
        return List.of();
    }

}