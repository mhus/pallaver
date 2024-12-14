package de.mhus.pallaver.quality;

import de.mhus.pallaver.model.LLModel;

public interface QualityCheck {

    String getTitle();

    void run(LLModel model, QualityCheckMonitor monitor) throws Exception;

}
