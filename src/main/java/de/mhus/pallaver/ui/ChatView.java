package de.mhus.pallaver.ui;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "chat", layout = MainLayout.class)
@PageTitle("Chat")
public class ChatView extends VerticalLayout {

    @Autowired
    private ModelService modelService;
    private TextArea chatHistory;
    private TextArea chatInput;
    private Span infoText;

    @PostConstruct
    public void init() {
        var menuBar = createMenuBar();
        infoText = new Span("");
        chatHistory = new TextArea();
        chatHistory.setSizeFull();
        chatHistory.setReadOnly(true);
        chatInput = new TextArea();
        chatInput.setSizeFull();
        chatInput.setPlaceholder("Enter your message here");
        chatInput.setValueChangeMode(ValueChangeMode.EAGER);
        chatInput.addKeyPressListener(
                e -> {
                    if (e.getKey().equals(Key.ENTER) && e.getModifiers().contains(KeyModifier.CONTROL)) {
                        actionSendMessage();
                    }
                });


        var splitLayout = new SplitLayout(chatHistory, chatInput);
        splitLayout.setSizeFull();
        splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
        splitLayout.setSplitterPosition(80);

        add(menuBar, infoText, splitLayout, new Text("Press Control+Enter to submit"));
        setSizeFull();
    }

    private void actionSendMessage() {
        addChatMessage("You", chatInput.getValue());
        chatInput.setValue("");
    }

    private void addChatMessage(String person, String value) {
        chatHistory.setValue(chatHistory.getValue() + person + ":\n" + value + "\n\n");
    }

    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        menuBar.addItem(VaadinIcon.RECYCLE.create(), e -> actionReset());
        var menuModel = menuBar.addItem("Model").getSubMenu();
        modelService.getModels().forEach(model -> {
            menuModel.addItem(model.getTitle(), e -> actionChangeModel(model));
        });
        return menuBar;
    }

    private void actionReset() {
    }

    private void actionChangeModel(LLModel model) {
        infoText.setText("Model: " + model.getTitle());

    }

}
