package de.mhus.pallaver.generator;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.ui.GeneratorMonitor;

public interface Generator {
    String getTitle();

    void run(LLModel model, GeneratorMonitor monitor);
}
