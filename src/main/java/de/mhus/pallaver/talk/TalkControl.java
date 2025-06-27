package de.mhus.pallaver.talk;

import de.mhus.pallaver.model.ModelControl;

public interface TalkControl extends ModelControl {

    void stop();

    boolean isStopped();
}
