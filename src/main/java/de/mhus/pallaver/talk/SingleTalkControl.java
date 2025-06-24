package de.mhus.pallaver.talk;

import de.mhus.pallaver.chat.BubbleFactory;
import de.mhus.pallaver.chat.ChatOptions;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.model.SingleModelControl;
import de.mhus.pallaver.ui.Bubble;

public class SingleTalkControl extends SingleModelControl {

    private final BubbleFactory bubbleFactory;

    public SingleTalkControl(LLModel model, ModelService modelService, BubbleFactory bubbleFactory) {
        super(model, modelService, createChatOptions(model, modelService));
        this.bubbleFactory = bubbleFactory;
    }

    protected static ChatOptions createChatOptions(LLModel model, ModelService modelService) {
        ChatOptions options = new ChatOptions();
        return options;
    }

    @Override
    protected Bubble addChatBubble(String title) {
        return bubbleFactory.createBubble(title);
    }

}
