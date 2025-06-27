package de.mhus.pallaver.talk;

import de.mhus.pallaver.model.ModelControl;

public abstract class EnhancedTalkControl implements TalkControl  {

    private volatile boolean stopped;

    public void stop() {
        stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

}
