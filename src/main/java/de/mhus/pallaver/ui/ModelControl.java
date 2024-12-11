package de.mhus.pallaver.ui;

import com.vaadin.flow.component.contextmenu.MenuItem;
import de.mhus.commons.tools.MString;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
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

    LLModel model;
    private final ModelService modelService;
    MenuItem item;
    @Getter
    volatile boolean enabled = false;
    private StreamingChatLanguageModel streamChatModel;
    private ChatLanguageModel chatModel;
    private TokenWindowChatMemory chatMemory;
    @Setter
    private ChatOptions chatOptions = new ChatOptions();
    private StreamChatAssistant streamChatAssistant;
    private ChatAssistant chatAssistant;

    public ModelControl(LLModel model, ModelService modelService, ChatOptions chatOptions) {
        this.model = model;
        this.modelService = modelService;
        this.chatOptions = chatOptions;
    }

    public String getTitle() {
        return model.getTitle();
    }

    public CompletableFuture<AiMessage> answer(String userMessage) {

        var otherBubble = addChatBubble(model.getTitle());

        try {

            chatMemory = TokenWindowChatMemory.withMaxTokens(chatOptions.getMaxTokens(), modelService.createTokenizer(model));
            if (MString.isSet(chatOptions.getPrompt()))
                chatMemory.add(SystemMessage.from(chatOptions.getPrompt()));

            boolean modeStream = chatOptions.getMode() == ChatOptions.MODE.STREAM // force streaming by config
                    ||
                    modelService.supports(model, LLM.STREAM) // if stream is supported and not tools
                    &&
                    (!chatOptions.isUseTools() || modelService.supports(model, LLM.STREAM_TOOLS)); // if tooling and stream tool is supported

            if (modeStream) {
                LOGGER.info("Use stream chat model: {} and tooling {}", model.getTitle(),chatOptions.isUseTools());
                if (streamChatModel == null) {
                    streamChatModel = modelService.createStreamingChatModel(model, chatOptions.getModelOptions());

                    streamChatAssistant = AiServices.builder(StreamChatAssistant.class)
                            .streamingChatLanguageModel(streamChatModel)
                            .chatMemory(MessageWindowChatMemory.withMaxMessages(chatOptions.getMaxMessages()))
                            .tools(createTools())
                            .build();

                }
            } else {
                LOGGER.info("Use chat model: {} and tooling {}", model.getTitle(),chatOptions.isUseTools());
                if (chatModel == null) {
                    chatModel = modelService.createChatModel(model, chatOptions.getModelOptions());

                    chatAssistant = AiServices.builder(ChatAssistant.class)
                            .chatLanguageModel(chatModel)
                            .chatMemory(MessageWindowChatMemory.withMaxMessages(chatOptions.getMaxMessages()))
                            .tools(createTools())
                            .build();
                }
            }

            chatMemory.add(UserMessage.userMessage(userMessage));
            CompletableFuture<AiMessage> futureAiMessage = new CompletableFuture<>();
            StreamingResponseHandler<AiMessage> handler = new StreamingResponseHandler<AiMessage>() {

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

            if (chatOptions.isUseTools() && modelService.supports(model, LLM.TOOLS)) {
                if (modelService.supports(model, LLM.STREAM_TOOLS)) {
                    TokenStream tokenStream = streamChatAssistant.generate(chatMemory.messages());
                    tokenStream.onNext(handler::onNext);
                    tokenStream.onComplete(handler::onComplete);
                    tokenStream.onError(handler::onError);
                    tokenStream.start();
                } else {
                    String answer = chatAssistant.generate(userMessage);
                    handler.onNext(answer);
                    handler.onComplete(Response.from(AiMessage.from(answer)));
                }
            } else {
                if (modelService.supports(model, LLM.STREAM)) {
                    streamChatModel.generate(chatMemory.messages(), handler);
                } else {
                    var answer = chatModel.generate(chatMemory.messages());
                    handler.onNext(answer.content().text());
                    handler.onComplete(answer);
                }
            }
            chatMemory.add(futureAiMessage.get());

            return futureAiMessage;
        } catch (Exception e) {
            LOGGER.error("Error", e);
            otherBubble.appendText("Error: " + e.getMessage());
            return CompletableFuture.completedFuture(AiMessage.from("Error: " + e.getMessage()));
        }
    }

    public void reset(ChatOptions options) {
        if (chatMemory != null)
            chatMemory.clear();
        streamChatModel = null;
        chatModel = null;
        chatMemory = null;
        streamChatAssistant = null;
        chatAssistant = null;
        this.chatOptions = options;
//        if (MString.isSet(chatOptions.getPrompt()))
//            chatHistory.addBubble("Prompt", true, ChatPanel.COLOR.YELLOW).setText(chatOptions.getPrompt());
    }

    public boolean isDefault() {
        return model.isDefault();
    }

    protected abstract Bubble addChatBubble(String title);

    protected abstract List<Object> createTools();

}