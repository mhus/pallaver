package de.mhus.pallaver.quality;

import de.mhus.pallaver.ui.Bubble;

public interface QualityCheckMonitor extends Bubble {

    QualityCheckTestMonitor forTest(String name);

    interface QualityCheckTestMonitor {
        void reportError(Throwable e);
        void reportError(String error);
        void setResult(boolean success);

        Bubble getBubble();
    }
}
