package de.mhus.pallaver.chat;

import de.mhus.pallaver.capture.CaptureService;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.model.SingleModelControl;
import de.mhus.pallaver.ui.Bubble;
import de.mhus.pallaver.model.ModelControl;

public class ChatModelControl extends SingleModelControl {

    private final BubbleFactory bubbleFactory;

    public ChatModelControl(LLModel model, ModelService modelService, ChatOptions chatOptions, CaptureService captureService, BubbleFactory bubbleFactory) {
        super(model, modelService, chatOptions, captureService);
        this.bubbleFactory = bubbleFactory;
    }

    @Override
    protected Bubble addChatBubble(String title) {
        return bubbleFactory.createBubble(title);
    }

}
