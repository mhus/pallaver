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
    private ModelOptions modelOptions = new ModelOptions();
    private int maxTokens = 1000;
    private String prompt = "";
}
