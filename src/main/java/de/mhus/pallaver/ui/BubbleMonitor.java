package de.mhus.pallaver.ui;

import com.vaadin.flow.component.UI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class BubbleMonitor extends ChatBubble implements QualityCheckMonitor {

    private final UI ui;

    public BubbleMonitor(UI ui, String title, ChatPanel.COLOR color) {
        super(title, false, color);
        this.ui = ui;
    }

    @Override
    public void setText(String text) {
        ui.access(() -> super.setText(text));
    }

    @Override
    public void appendText(String text) {
        ui.access(() -> super.appendText(text));
    }

    @Override
    public QualityCheckTestMonitor forTest(String name) {
        return new BubbleQualityCheckTestMonitor();
    }

    private class BubbleQualityCheckTestMonitor implements QualityCheckMonitor.QualityCheckTestMonitor {
        private boolean success = true;

        @Override
        public void reportError(Throwable e) {
            LOGGER.warn("Error", e);
            appendText("Error: " + e.getMessage());
            success = false;
        }

        @Override
        public void reportError(String error) {
            LOGGER.warn("Error: {}", error);
            appendText("Error: " + error);
            success = false;
        }

        @Override
        public void setResult(boolean success) {
            LOGGER.info("Result: {}", success);
            this.success = success;
        }

        @Override
        public Bubble getBubble() {
            return BubbleMonitor.this;
        }

    }
}