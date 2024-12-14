package de.mhus.pallaver.generator;

import de.mhus.pallaver.model.LLModel;

public interface Generator {
    String getTitle();

    void run(LLModel model, GeneratorMonitor monitor);
}
