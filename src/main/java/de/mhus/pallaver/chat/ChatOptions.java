package de.mhus.pallaver.chat;

import de.mhus.pallaver.model.ModelOptions;
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
    private int maxTokens = 0;
    @Builder.Default
    private int maxMessages = 30;
    @Builder.Default
    private String prompt = "";
    @Builder.Default
    private boolean useTools = false;
    @Builder.Default
    private MODE mode = MODE.AUTO;

}
