package de.mhus.pallaver.ui;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.mhus.pallaver.chat.BubbleFactory;
import de.mhus.pallaver.chat.ChatModelControlFactory;
import de.mhus.pallaver.chat.SimpleChatAssistantFactory;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Route(value = "chat", layout = MainLayout.class)
@PageTitle("Chat")
public class ChatView extends VerticalLayout {

    @Autowired
    private ModelService modelService;
    private ChatPanel chatHistory;
    private TextArea chatInput;
    private Span infoText;
    private List<ModelItem> models = new ArrayList<>();
    private ChatOptions chatOptions = new ChatOptions();
    @Autowired
    private List<ChatModelControlFactory> controlFactories;
    @Autowired
    private SimpleChatAssistantFactory simpleChatAssistantFactory;
    private ChatModelControlFactory modelControlFactory;

    @PostConstruct
    public void init() {
        modelControlFactory = simpleChatAssistantFactory;
        var menuBar = createMenuBar();
        infoText = new Span("");
        chatHistory = new ChatPanel();
        chatHistory.setSizeFull();
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

        var sendBtn = new Button("Press Control+Enter to submit", e -> actionSendMessage());
        sendBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);


        add(menuBar, infoText, splitLayout, sendBtn);
        setSizeFull();

        models.stream().filter(ModelItem::isDefault).forEach(m -> {
            m.item.setChecked(true);
            m.enabled = true;
        });
        updateModelText();
    }

    private void actionSendMessage() {
        var userMessage = chatInput.getValue();
        addChatBubble("You", true, ChatPanel.COLOR.BLUE).setText(userMessage);
        chatHistory.scrollToEnd();
        chatInput.setValue("");
        chatInput.setReadOnly(true);
        ColorRotator colorRotator = new ColorRotator();
        UI ui = UI.getCurrent();
        models.stream().filter(ModelItem::isEnabled).forEach(m -> {
            Thread.startVirtualThread(() -> m.answer(userMessage, colorRotator.next(), ui));
        });
        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error("Error", e);
            }
            ui.access(() -> chatInput.setReadOnly(false));
        });
    }

    private ChatBubble addChatBubble(String person, boolean left, ChatPanel.COLOR color) {
        return chatHistory.addBubble(person, left, color);
    }

    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        menuBar.addItem(VaadinIcon.RECYCLE.create(), e -> actionReset());
        var menuModel = menuBar.addItem("Model").getSubMenu();
        modelService.getModels().forEach(model -> {
            var item = menuModel.addItem(model.getTitle());
            item.setCheckable(true);
            item.setChecked(false);
            models.add(new ModelItem(model, item));
        });
        menuBar.addItem("Options", e -> actionShowOptions());

        var menuControl = menuBar.addItem("Control").getSubMenu();
        controlFactories.forEach(factory -> {
            var item = menuControl.addItem(factory.getTitle());
            item.setCheckable(true);
            item.setChecked(factory == simpleChatAssistantFactory);
            item.addClickListener(e -> {
                modelControlFactory = factory;
                menuControl.getItems().forEach(i -> i.setChecked( i == e.getSource() ));
                updateModelText();
                actionReset();
                chatInput.setValue(modelControlFactory.getDefaultPrompt());
            });
        });


        return menuBar;
    }

    private void actionShowOptions() {
        var dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        var optionsClone = chatOptions.toBuilder().build();
        dialog.add(createOptionsForm(optionsClone));
        dialog.setHeaderTitle("Chat Options");
        dialog.getFooter().add(new Button("Cancel", e -> dialog.close()));
        dialog.getFooter().add(new Button("Save", e -> {
            dialog.close();
            chatOptions = optionsClone;
            actionReset();
        }));
        dialog.open();
    }

    private FormLayout createOptionsForm(ChatOptions options) {
        var formLayout = new FormLayout();
        var tPrompt = new TextArea("Prompt");
        tPrompt.setValue(options.getPrompt());
        tPrompt.addValueChangeListener(e -> options.setPrompt(e.getValue()));
        formLayout.add(tPrompt);

        var tMaxTokens = new NumberField("Max Tokens");
        tMaxTokens.setValue((double) options.getMaxTokens());
        tMaxTokens.setMin(1);
        tMaxTokens.setStep(1);
        tMaxTokens.addValueChangeListener(e -> options.setMaxTokens(e.getValue().intValue()));
        formLayout.add(tMaxTokens);

        var tTemperature = new NumberField("Temperature");
        tTemperature.setValue(options.getModelOptions().getTemperature());
        tTemperature.setMin(0);
        tTemperature.setStep(0.01);
        tTemperature.setMax(1);
        tTemperature.addValueChangeListener(e -> options.getModelOptions().setTemperature(e.getValue()));
        formLayout.add(tTemperature);

        var tTimeout = new NumberField("Timeout");
        tTimeout.setValue(options.getModelOptions().getTimeoutInSeconds() == null ? 60 : options.getModelOptions().getTimeoutInSeconds().doubleValue());
        tTimeout.setMin(1);
        tTimeout.setStep(1);
        tTimeout.setMax(3600);
        tTimeout.addValueChangeListener(e -> options.getModelOptions().setTimeoutInSeconds(e.getValue().intValue()));
        formLayout.add(tTimeout);

        var tSeed = new NumberField("Seed");
        tSeed.setValue(options.getModelOptions().getSeed() == null ? 0 : options.getModelOptions().getSeed().doubleValue());
        tSeed.setMin(0);
        tSeed.setStep(1);
        tSeed.addValueChangeListener(e -> options.getModelOptions().setSeed(e.getValue().intValue()));
        formLayout.add(tSeed);

        var tFormat = new TextArea("Format");
        tFormat.setValue(options.getModelOptions().getFormat());
        tFormat.addValueChangeListener(e -> options.getModelOptions().setFormat(e.getValue()));
        formLayout.add(tFormat);

        var checkboxUseTools = new Checkbox("Use Tools");
        checkboxUseTools.setValue(options.isUseTools());
        checkboxUseTools.addValueChangeListener(e -> options.setUseTools(e.getValue()));
        formLayout.add(checkboxUseTools);

        formLayout.setSizeFull();
        return formLayout;
    }

    private void actionReset() {
        models.forEach(model -> model.reset());
        updateModelText();
        chatHistory.clear();
        chatInput.setValue("");
    }

    private void updateModelText() {
        final StringBuffer text = new StringBuffer();
        models.stream().filter(ModelItem::isEnabled).forEach(m -> text.append(m.getTitle()).append(" "));
        infoText.setText("Control: " + modelControlFactory.getTitle() + ", Models: " + text);
    }

    @Getter
    private class ModelItem {
        MenuItem item;
        LLModel model;
        boolean enabled;

        private ModelControl control;

        public ModelItem(LLModel model, MenuItem item) {
            this.model = model;
            this.item = item;
            item.addClickListener(e -> {
                enabled = item.isChecked();
                updateModelText();
            });
        }

        public boolean isDefault() {
            return model.isDefault();
        }

        public void reset() {
            if (control != null)
                control.reset(null);
            control = null;
        }

        public String getTitle() {
            return model.getTitle();
        }

        public void answer(String userMessage, ChatPanel.COLOR color, UI ui) {
            if (control == null) {
                control = modelControlFactory.createModelControl(model, chatOptions, new ChatViewBubbleFactory(color, ui));
            }
            control.answer(userMessage);
        }

    }

    private class ChatViewBubbleFactory implements BubbleFactory {

        private final ChatPanel.COLOR color;
        private final UI ui;

        ChatViewBubbleFactory(ChatPanel.COLOR color, UI ui) {
            this.color = color;
            this.ui = ui;
        }

        @Override
        public Bubble createBubble(String title) {
            var bubble = new ChatBubble(title, false, color) {
                public void appendText(String text) {
                    ui.access(() -> {
                        super.appendText(text);
                    });
                }

                @Override
                public void onComplete() {
                    ui.access(() -> {
                        chatHistory.scrollToEnd();
                    });
                }
            };
            ui.access(() -> chatHistory.addBubble(bubble));
            return bubble;
        }
    }
}
