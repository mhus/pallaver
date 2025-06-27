package de.mhus.pallaver.chat;

import de.mhus.pallaver.capture.CaptureService;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.tools.JavaScriptTool;
import de.mhus.pallaver.tools.SimpleCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultChatFactory implements ChatModelControlFactory {

    @Autowired
    private ModelService modelService;

    @Autowired
    private CaptureService captureService;

    @Override
    public String getTitle() {
        return "Default";
    }

    @Override
    public ChatModelControl createModelControl(LLModel model, ChatOptions chatOptions, BubbleFactory bubbleFactory) {
        return new SimpleChatModelControl(model, modelService, chatOptions, captureService, bubbleFactory);
    }

    @Override
    public String getDefaultPrompt() {
        return "";
    }

    private static class SimpleChatModelControl extends ChatModelControl {

        public SimpleChatModelControl(LLModel model, ModelService modelService, ChatOptions chatOptions, CaptureService captureService, BubbleFactory bubbleFactory) {
            super(model, modelService, chatOptions, captureService, bubbleFactory);
        }

        @Override
        protected List<Object> createTools() {
            JavaScriptTool jsTool = new JavaScriptTool();
            SimpleCalculator calculatorTool = new SimpleCalculator();
            return List.of(jsTool, calculatorTool);
        }

    }
}
