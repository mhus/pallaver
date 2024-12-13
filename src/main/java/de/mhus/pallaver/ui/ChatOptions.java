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
    @Builder.Default
    private ModelOptions modelOptions = new ModelOptions();
    @Builder.Default
    private int maxTokens = 1000;
    @Builder.Default
    private int maxMessages = 30;
    @Builder.Default
    private String prompt = "";
    @Builder.Default
    private boolean useTools = false;
    @Builder.Default
    private MODE mode = MODE.AUTO;

}
