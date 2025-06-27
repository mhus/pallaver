package de.mhus.pallaver.talk;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.mhus.commons.io.CSVReader;
import de.mhus.commons.tools.MString;
import de.mhus.pallaver.chat.BubbleFactory;
import de.mhus.pallaver.chat.ChatOptions;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelControl;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.ui.Bubble;
import de.mhus.pallaver.ui.ChatBubble;
import de.mhus.pallaver.ui.ChatHistoryPanel;
import de.mhus.pallaver.ui.ColorRotator;
import de.mhus.pallaver.ui.MainLayout;
import elemental.json.JsonType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.EOFException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Route(value = "talk", layout = MainLayout.class)
@PageTitle("Talk")
public class TalkView extends VerticalLayout {

    @Autowired
    private ModelService modelService;
    private ChatHistoryPanel chatHistory;
    private TextArea chatInput;
    private Span infoText;
    private List<MyModelItem> modelItems = new ArrayList<>();
    private MyModelItem selectedModelItem;

    @Autowired
    private List<TalkControlFactory> controlFactories;
    @Autowired
    private DefaultTalkFactory defaultModelControlFactory;
    private TalkControlFactory selectedModelControlFactory;
    private MenuItem btnStop;
    private MenuItem btnModel;
    private MenuItem btnControl;
    private Button btnSend;
    private Button btnInputPromptMenu;

    @PostConstruct
    public void init() {
        selectedModelControlFactory = defaultModelControlFactory;
        var menuBar = createMenuBar();
        infoText = new Span("");
        chatHistory = new ChatHistoryPanel();
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

        btnSend = new Button("Press Control+Enter to submit", e -> actionSendMessage());
        btnSend.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        btnInputPromptMenu = new Button("Prompt");
        var inputPromptMenu = new ContextMenu();
        inputPromptMenu.setOpenOnClick(true);
        inputPromptMenu.setTarget(btnInputPromptMenu);
        CSVReader reader = new CSVReader(new InputStreamReader(getClass().getResourceAsStream("/input-prompts.csv")));
        try {
            reader.readHeader(true);
            while (reader.next()) {
                if (reader.getCurrentLine().length == 0)
                    continue;
                var prompt = reader.get("prompt").replaceAll("\\\\n", "\n");
                inputPromptMenu.addItem(reader.get("title"), e -> chatInput.setValue(prompt));
            }
        } catch (EOFException e) {
            LOGGER.debug("End of file /input-prompts.csv");
        } catch (Exception e) {
            LOGGER.error("Error", e);
        }

        var btnLayout = new HorizontalLayout(btnSend, btnInputPromptMenu);
        btnLayout.setWidthFull();
        btnLayout.setMargin(false);
        btnLayout.setPadding(false);

        add(menuBar, infoText, splitLayout, btnLayout);
        setSizeFull();

        updateModelText();

        modelItems.stream().filter(m -> m.getModel().isDefault()).findFirst().ifPresent(m -> {
            selectedModelItem = m;
            m.getItem().setChecked(true);
            updateModelText();
        });

    }

    private void actionSendMessage() {
        chatInput.getElement().executeJs("return this.inputElement.value.substring(this.inputElement.selectionStart,this.inputElement.selectionEnd)").then(s -> {
            actionSendMessage(s == null || s.getType() == JsonType.NULL || MString.isEmpty(s.asString()) ? chatInput.getValue() : s.asString());
        });
    }

    private void actionSendMessage(String userMessage) {
        addChatBubble("You", true, ChatHistoryPanel.COLOR.BLUE).setText(userMessage);
        chatHistory.scrollToEnd();
        chatInput.setReadOnly(true);
        ColorRotator colorRotator = new ColorRotator();
        UI ui = UI.getCurrent();
        setRunning(true);
        Thread.startVirtualThread(() -> selectedModelItem.answer(selectedModelControlFactory, userMessage, colorRotator.next(), ui));
        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error("Error", e);
            }
            ui.access(() -> chatInput.setReadOnly(false));
        });
    }

    private void setRunning(boolean running) {
        btnStop.setEnabled(running);
        btnModel.setEnabled(!running);
        btnControl.setEnabled(!running);
        btnSend.setEnabled(!running);
        btnInputPromptMenu.setEnabled(!running);
    }

    private ChatBubble addChatBubble(String person, boolean left, ChatHistoryPanel.COLOR color) {
        return chatHistory.addBubble(person, left, color);
    }

    private MenuBar createMenuBar() {
        var menuBar = new MenuBar();
        menuBar.addItem(VaadinIcon.RECYCLE.create(), e -> actionReset());
        btnModel = menuBar.addItem("Model");
        var menuModel = btnModel.getSubMenu();
        modelService.getModels().forEach(model -> {
            var item = menuModel.addItem(model.getTitle());
            item.setCheckable(true);
            item.setChecked(false);
            modelItems.add(new MyModelItem(model, item));
        });

        btnControl = menuBar.addItem("Control");
        var menuControl = btnControl.getSubMenu();
        controlFactories.forEach(factory -> {
            var item = menuControl.addItem(factory.getTitle());
            item.setCheckable(true);
            item.setChecked(factory == defaultModelControlFactory);
            item.addClickListener(e -> {
                selectedModelControlFactory = factory;
                menuControl.getItems().forEach(i -> i.setChecked( i == e.getSource() ));
                updateModelText();
                actionReset();
                chatInput.setValue(selectedModelControlFactory.getDefaultPrompt());
            });
        });

        btnStop = menuBar.addItem("Stop", e -> stopExecution());
        btnStop.setEnabled(false);


//        var menuPrompt = menuBar.addItem("Prompt").getSubMenu();
//        menuPrompt.addItem("awesome-chatgpt-prompts", e -> actionSelectPrompt("awesome-chatgpt-prompts", "https://raw.githubusercontent.com/f/awesome-chatgpt-prompts/refs/heads/main/prompts.csv"));


        return menuBar;
    }

    private void stopExecution() {
        selectedModelItem.stop();
    }

//    private void actionSelectPrompt(String title, String url) {
//
//    }

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
        if (selectedModelItem != null) {
            selectedModelItem.reset();
        }
        updateModelText();
        chatHistory.clear();
        chatInput.setValue("");
    }

    private void updateModelText() {
        infoText.setText("Control: " + selectedModelControlFactory.getTitle() + ", Model: " + (selectedModelItem == null ? "" : selectedModelItem.getTitle()));
    }

    @Getter
    private class MyModelItem {
        private final LLModel model;
        private TalkControl control;
        MenuItem item;
        @Setter
        private ChatHistoryPanel.COLOR color = ChatHistoryPanel.COLOR.GREEN;

        public MyModelItem(LLModel model, MenuItem item) {
            this.model = model;
            this.item = item;

            item.addClickListener(e -> {
                if (selectedModelItem != null)
                    selectedModelItem.reset();
                reset();
                selectedModelItem = this;
                modelItems.forEach(m -> m.getItem().setChecked(m == selectedModelItem));
                updateModelText();
            });
        }

        public String getTitle() {
            return model.getTitle();
        }

        public boolean isDefault() {
            return model.isDefault();
        }

        public void answer(TalkControlFactory selectedModelControlFactory, String userMessage, ChatHistoryPanel.COLOR next, UI ui) {
            if (control == null) {
                control = selectedModelControlFactory.createModelControl(model, new ChatViewBubbleFactory(color, ui));
            }
            control.answer(userMessage);
        }

        public void reset() {
            if (control != null)
                control.reset(null);
            control = null;
        }

        public void stop() {
            if (control != null) {
                control.stop();
            }
            setRunning(false);
            chatInput.setReadOnly(false);
            chatInput.setValue("");
            chatHistory.scrollToEnd();
        }
    }

    private class ChatViewBubbleFactory implements BubbleFactory {

        private final ChatHistoryPanel.COLOR color;
        private final UI ui;

        ChatViewBubbleFactory(ChatHistoryPanel.COLOR color, UI ui) {
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
