package de.mhus.pallaver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LLModel {
    String title;
    String type;
    boolean isDefault;
}
