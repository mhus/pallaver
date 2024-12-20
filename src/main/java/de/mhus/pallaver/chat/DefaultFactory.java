package de.mhus.pallaver.chat;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.tools.JavaScriptTool;
import de.mhus.pallaver.tools.SimpleCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultFactory implements ChatModelControlFactory {

    @Autowired
    private ModelService modelService;

    @Override
    public String getTitle() {
        return "Default";
    }

    @Override
    public ChatModelControl createModelControl(LLModel model, ChatOptions chatOptions, BubbleFactory bubbleFactory) {
        return new SimpleChatModelControl(model, modelService, chatOptions, bubbleFactory);
    }

    @Override
    public String getDefaultPrompt() {
        return "";
    }

    private static class SimpleChatModelControl extends ChatModelControl {

        public SimpleChatModelControl(LLModel model, ModelService modelService, ChatOptions chatOptions, BubbleFactory bubbleFactory) {
            super(model, modelService, chatOptions, bubbleFactory);
        }

        @Override
        protected List<Object> createTools() {
            JavaScriptTool jsTool = new JavaScriptTool();
            SimpleCalculator calculatorTool = new SimpleCalculator();
            return List.of(jsTool, calculatorTool);
        }

    }
}
