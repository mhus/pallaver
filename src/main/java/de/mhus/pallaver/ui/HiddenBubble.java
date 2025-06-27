package de.mhus.pallaver.ui;

import de.mhus.pallaver.chat.BubbleFactory;
import lombok.Getter;

public class HiddenBubble implements Bubble {

    private final Bubble output;
    private String title;
    private StringBuffer text = new StringBuffer();
    private String progress = "";
    @Getter
    private boolean completed = false;

    public static BubbleFactory factory(Bubble output, char progress) {
        return (title) -> new HiddenBubble(output, title, progress);
    }

    public static BubbleFactory factory(char progress) {
        return (title) -> new HiddenBubble(new Bubble() {
            @Override
            public void appendText(String token) {
                System.out.print(token);
            }

            @Override
            public void onComplete() {
                System.out.println();
            }
        }, title, progress);
    }

    public static BubbleFactory factory() {
        return (title) -> new HiddenBubble(title);
    }

    public HiddenBubble(Bubble output, String title, char progress) {
        this.progress = progress + " ";
        this.title = title;
        this.output = output;
        if (output != null) {
            output.appendText(title + ": ");
        }
    }

    public HiddenBubble(String title) {
        this(null, title, '\0');
    }

    public HiddenBubble() {
        this(null, "?", '\0');
    }

    @Override
    public void appendText(String token) {
        if (output != null) {
            output.appendText(progress);
        }
        if (completed) return;
        text.append(token);
    }

    @Override
    public void onComplete() {
        if (output != null) {
            output.appendText(" Size: " + text.length() + "\n\n");
        }
        completed = true;
    }

    public String getText() {
        return text.toString();
    }

    @Override
    public String toString() {
        return "HiddenBubble: %s size %d".formatted(title, text.length());
    }
}
