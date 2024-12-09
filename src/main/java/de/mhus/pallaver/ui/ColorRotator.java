package de.mhus.pallaver.ui;

public class ColorRotator {
    private int index = 0;
    private ChatPanel.COLOR[] colors;

    public ColorRotator(ChatPanel.COLOR ... colors) {
        this.colors = colors;
    }

    public ChatPanel.COLOR next() {
        ChatPanel.COLOR out = colors[index];
        index++;
        if (index >= colors.length) index = 0;
        return out;
    }
}