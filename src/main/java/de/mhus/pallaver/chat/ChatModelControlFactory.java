package de.mhus.pallaver.chat;

import de.mhus.pallaver.model.LLModel;

public interface ChatModelControlFactory {

    String getTitle();

    ChatModelControl createModelControl(LLModel model, ChatOptions chatOptions, BubbleFactory bubbleFactory);

    String getDefaultPrompt();
}
