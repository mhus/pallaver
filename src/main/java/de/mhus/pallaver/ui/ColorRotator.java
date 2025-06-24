package de.mhus.pallaver.ui;

public class ColorRotator {
    private int index = 0;
    private ChatHistoryPanel.COLOR[] colors;

    public ColorRotator(ChatHistoryPanel.COLOR ... colors) {
        if (colors == null || colors.length == 0) colors = new ChatHistoryPanel.COLOR[] { ChatHistoryPanel.COLOR.GREEN, ChatHistoryPanel.COLOR.RED, ChatHistoryPanel.COLOR.YELLOW };
        this.colors = colors;
    }

    public ChatHistoryPanel.COLOR next() {
        ChatHistoryPanel.COLOR out = colors[index];
        index++;
        if (index >= colors.length) index = 0;
        return out;
    }
}
