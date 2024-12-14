package de.mhus.pallaver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelOptions {
    @Builder.Default
    private double temperature = 0.2;
    @Builder.Default
    private String format = "";
    @Builder.Default
    private Integer seed = null;
    @Builder.Default
    private Integer timeoutInSeconds = null;
    @Builder.Default
    private boolean logging = true;

}
