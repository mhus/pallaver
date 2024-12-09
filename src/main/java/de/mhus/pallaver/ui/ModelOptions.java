package de.mhus.pallaver.ui;

import lombok.Data;

@Data
public class ModelOptions {
    private double temperature = 0.2;
    private String format = "";
    private Integer seed = null;
    private Integer timeoutInSeconds = null;

}
