package de.mhus.pallaver.ui;

public interface QualityCheckMonitor extends Bubble {

    QualityCheckTestMonitor forTest(String name);

    interface QualityCheckTestMonitor {
        void reportError(Throwable e);
        void reportError(String error);
        void setResult(boolean success);

        Bubble getBubble();
    }
}
