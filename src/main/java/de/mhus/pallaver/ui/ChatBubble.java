package de.mhus.pallaver.ui;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import lombok.Getter;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class ChatBubble extends Div {
    private final ChatPanel.COLOR color;
    private final Div titleDiv;
    private final Div textDiv;
    private final Html textHtml;
    @Getter
    private String text;
    private Parser parser = Parser.builder().build();
    HtmlRenderer renderer = HtmlRenderer.builder().build();

    public ChatBubble(String title, boolean left, ChatPanel.COLOR color) {
        this.color = color;
        titleDiv = new Div(title);
        add(titleDiv);
        textHtml = new Html(toHtml(""));
        textDiv = new Div(textHtml);
        add(textDiv);
        this.text = "";

        addClassNames("bubble", "bubble-" + color.name().toLowerCase());
        if (left)
            addClassNames("bubble-left");
        else
            addClassNames("bubble-right");
        titleDiv.addClassName("bubble-title");
        textDiv.addClassName("bubble-text");


    }

    public void setText(String text) {
        this.text = text;
        textHtml.setHtmlContent(toHtml(text));
    }

    private String toHtml(String text) {
        Node document = parser.parse(text);
        String html = renderer.render(document);
        return "<div>" + html + "</div>";
//        return "<p>" + text
//                .replaceAll("&", "&amp;")
//                .replaceAll("<", "&lt;")
//                .replaceAll(">", "&gt;")
//                .replaceAll("\n", "<br>")
//                + "</p>";
    }

    public ChatBubble appendText(String text) {
        this.text += text;
        textHtml.setHtmlContent(toHtml(this.text));
        return this;
    }

}
