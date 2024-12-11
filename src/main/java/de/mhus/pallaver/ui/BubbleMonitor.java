package de.mhus.pallaver.ui;

import com.vaadin.flow.component.UI;
import lombok.Getter;

@Getter
public class BubbleMonitor extends ChatBubble implements QualityCheckMonitor {

    private final UI ui;
    private boolean success = true;

    public BubbleMonitor(UI ui, String title, ChatPanel.COLOR color) {
        super(title, false, color);
        this.ui = ui;
    }

    @Override
    public void reportError(Throwable e) {
        appendText("Error: " + e.getMessage());
        success = false;
    }

    @Override
    public void reportError(String error) {
        appendText("Error: " + error);
        success = false;
    }

    @Override
    public void setResult(boolean success) {
        this.success = success;
    }

    @Override
    public void setText(String text) {
        ui.access(() -> super.setText(text));
    }

    @Override
    public void appendText(String text) {
        ui.access(() -> super.appendText(text));
    }

}