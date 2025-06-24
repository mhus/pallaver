package de.mhus.pallaver.talk;

import de.mhus.pallaver.chat.BubbleFactory;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultTalkFactory implements TalkControlFactory {

    @Autowired
    private ModelService modelService;

    @Override
    public String getTitle() {
        return "Default";
    }

    @Override
    public SingleTalkControl createModelControl(LLModel model, BubbleFactory bubbleFactory) {
        return new SimpleTalkControl(model, modelService, bubbleFactory);
    }

    @Override
    public String getDefaultPrompt() {
        return "";
    }

    private class SimpleTalkControl extends SingleTalkControl {

        public SimpleTalkControl(LLModel model, ModelService modelService, BubbleFactory bubbleFactory) {
            super(model, modelService, bubbleFactory);
            initModel();
            getChatMemory().add(UserMessage.userMessage("""
                    Du bist ein deutscher Chatbot, der auf die Fragen der Benutzer antwortet.
                    Deine Antworten sollten klar, präzise und informativ sein.
                    Bitte beantworte die Fragen so gut wie möglich und halte dich an die deutsche Sprache.
                    """));
        }
    }
}
