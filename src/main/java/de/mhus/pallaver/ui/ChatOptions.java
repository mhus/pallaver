package de.mhus.pallaver.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder=true)
public class ChatOptions {
    public enum MODE {
        AUTO,
        CHAT,
        STREAM
    }
    private ModelOptions modelOptions = new ModelOptions();
    private int maxTokens = 1000;
    private int maxMessages = 30;
    private String prompt = "";
    private boolean useTools = false;
    private MODE mode = MODE.AUTO;

}
