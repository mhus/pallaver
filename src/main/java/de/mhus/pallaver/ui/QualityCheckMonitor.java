package de.mhus.pallaver.ui;

public interface QualityCheckMonitor extends Bubble {

    void reportError(Throwable e);
    void reportError(String error);
    void setResult(boolean success);
}
