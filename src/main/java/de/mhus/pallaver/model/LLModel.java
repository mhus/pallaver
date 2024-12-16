package de.mhus.pallaver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class LLModel {
    String title;
    String type;
    boolean isDefault;
    String url;
    String model;
    String apiKey;
}
