package de.mhus.pallaver.ui;

import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ChatHistoryPanel extends Scroller {

    public enum COLOR {
        BLUE,
        GREEN,
        RED,
        YELLOW
    }

    private final VerticalLayout content = new VerticalLayout();

    public ChatHistoryPanel() {
        setContent(content);
        addClassName("bubble-chat");
    }

    public ChatBubble addBubble(String personName, boolean left, COLOR color) {
        ChatBubble bubble = new ChatBubble(personName, left, color);
        addBubble(bubble);
        return bubble;
    }

    public ChatBubble addBubble(ChatBubble bubble) {
        content.add(bubble);
        return bubble;
    }

    public void clear() {
        content.removeAll();
    }

    public void scrollToEnd() {
        getElement().executeJs("this.scrollTop = this.scrollHeight;");
    }

}
