package de.mhus.pallaver.talk;

import de.mhus.pallaver.chat.BubbleFactory;
import de.mhus.pallaver.chat.ChatModelControl;
import de.mhus.pallaver.chat.ChatOptions;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelControl;

public interface TalkControlFactory {

    String getTitle();

    ModelControl createModelControl(LLModel model, BubbleFactory bubbleFactory);

    String getDefaultPrompt();
}
