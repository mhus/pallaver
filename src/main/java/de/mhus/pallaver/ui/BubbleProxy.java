package de.mhus.pallaver.ui;

import lombok.Getter;

@Getter
public class BubbleProxy implements Bubble {
    private final Bubble parent;
    private final StringBuilder text = new StringBuilder();
    private boolean compleated;

    public BubbleProxy(Bubble parent) {
        this.parent = parent;
    }

    @Override
    public void appendText(String token) {
        parent.appendText(token);
        text.append(token);
    }

    @Override
    public void onComplete() {
        parent.onComplete();
        compleated = true;
    }
}
