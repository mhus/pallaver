package de.mhus.pallaver.lltype;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

public class XChatModel {
    public XChatModel(StreamingChatModel model) {
    }

    public XChatModel(ChatModel model) {
    }
}
