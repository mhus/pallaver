package de.mhus.pallaver.quality;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.ui.QualityCheckMonitor;

public interface QualityCheck {

    String getTitle();

    void run(LLModel model, QualityCheckMonitor monitor) throws Exception;

}
