package de.mhus.pallaver.ui;

import com.vaadin.flow.component.UI;

public class GeneratorMonitor {
    private final UI ui;
    private final ChatPanel chatHistory;

    public GeneratorMonitor(UI ui, ChatPanel chatHistory) {
        this.ui = ui;
        this.chatHistory = chatHistory;
    }

    public void reportError(Exception e) {
        ui.access(() -> {
            chatHistory.addBubble("Error", true, ChatPanel.COLOR.RED).setText(e.getMessage());
            chatHistory.scrollToEnd();
        });
    }

    public Bubble createResultBubble(String title) {
        var bubble =  new MonitorChatBubble(title, false, ChatPanel.COLOR.YELLOW);
        ui.access(() -> {
            chatHistory.addBubble(bubble);
            chatHistory.scrollToEnd();
        });
        return bubble;
    }

    public Bubble createQuestionBubble(String title) {
        var bubble =  new MonitorChatBubble(title, true, ChatPanel.COLOR.BLUE);
        ui.access(() -> {
            chatHistory.addBubble(bubble);
            chatHistory.scrollToEnd();
        });
        return bubble;
    }

    class MonitorChatBubble extends ChatBubble {
        public MonitorChatBubble(String title, boolean left, ChatPanel.COLOR color) {
            super(title, left, color);
        }

        public void appendText(String text) {
            ui.access(() -> {
                super.appendText(text);
            });
        }

        public void setText(String text) {
            ui.access(() -> {
                super.setText(text);
            });
        }
    }
}
