package de.mhus.pallaver.model;

import de.mhus.commons.tools.MString;
import de.mhus.pallaver.chat.ChatAssistant;
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
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ModelControl {

    AiMessage answer(String userMessage);

    void reset(ChatOptions options);

}