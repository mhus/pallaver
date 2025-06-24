package de.mhus.pallaver.generator;

import com.vaadin.flow.component.UI;
import de.mhus.pallaver.ui.Bubble;
import de.mhus.pallaver.ui.ChatBubble;
import de.mhus.pallaver.ui.ChatHistoryPanel;

public class GeneratorMonitor {
    private final UI ui;
    private final ChatHistoryPanel chatHistory;

    public GeneratorMonitor(UI ui, ChatHistoryPanel chatHistory) {
        this.ui = ui;
        this.chatHistory = chatHistory;
    }

    public void reportError(Exception e) {
        ui.access(() -> {
            chatHistory.addBubble("Error", true, ChatHistoryPanel.COLOR.RED).setText(e.getMessage());
            chatHistory.scrollToEnd();
        });
    }

    public Bubble createResultBubble(String title) {
        var bubble =  new MonitorChatBubble(title, false, ChatHistoryPanel.COLOR.YELLOW);
        ui.access(() -> {
            chatHistory.addBubble(bubble);
            chatHistory.scrollToEnd();
        });
        return bubble;
    }

    public Bubble createQuestionBubble(String title) {
        var bubble =  new MonitorChatBubble(title, true, ChatHistoryPanel.COLOR.BLUE);
        ui.access(() -> {
            chatHistory.addBubble(bubble);
            chatHistory.scrollToEnd();
        });
        return bubble;
    }

    class MonitorChatBubble extends ChatBubble {
        public MonitorChatBubble(String title, boolean left, ChatHistoryPanel.COLOR color) {
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
