package de.mhus.pallaver.ui;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Route(value = "chat", layout = MainLayout.class)
@PageTitle("Chat")
public class ChatView extends VerticalLayout {

    @Autowired
    private ModelService modelService;
    private ChatPanel chatHistory;
    private TextArea chatInput;
    private Span infoText;
    private StreamingChatLanguageModel chatModel;
    private TokenWindowChatMemory chatMemory;
    private String chatModelName;

    @PostConstruct
    public void init() {
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

        try {
            actionChangeModel(modelService.getModels().iterator().next());
        } catch (Exception e) {
            infoText.setText("No model found");
        }
    }

    private void actionSendMessage() {
        var userMessage = chatInput.getValue();
        addChatBubble("You", true, ChatPanel.COLOR.BLUE).setText(userMessage);
        chatHistory.scrollToEnd();
        chatInput.setValue("");
        chatInput.setReadOnly(true);
        final var ui = UI.getCurrent();
        var otherBubble = addChatBubble(chatModelName, false, ChatPanel.COLOR.GREEN);
        Thread.startVirtualThread(() -> {
            try {
                chatMemory.add(UserMessage.userMessage(userMessage));
                CompletableFuture<AiMessage> futureAiMessage = new CompletableFuture<>();
                StreamingResponseHandler<AiMessage> handler = new StreamingResponseHandler<AiMessage>() {

                    @Override
                    public void onNext(String token) {
                        ui.access(() -> {
                            otherBubble.appendText(token);
                            chatHistory.scrollToEnd();
                        });
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        futureAiMessage.complete(response.content());
                        ui.access(() -> {
                            chatInput.setReadOnly(false);
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        LOGGER.error("Error", e);
                        ui.access(() -> {
                            otherBubble.appendText("Error: " + e.getMessage());
                            chatHistory.scrollToEnd();
                            chatInput.setReadOnly(false);
                        });
                    }
                };

                chatModel.generate(chatMemory.messages(), handler);
                chatMemory.add(futureAiMessage.get());

            } catch (Exception e) {
                LOGGER.error("Error", e);
                ui.access(() -> {
                    otherBubble.appendText("Error: " + e.getMessage());
                    chatInput.setReadOnly(false);
                });
            }
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
            menuModel.addItem(model.getTitle(), e -> actionChangeModel(model));
        });
        return menuBar;
    }

    private void actionReset() {
        chatMemory.clear();
        chatHistory.clear();
        infoText.setText("Model: " + chatModelName);
    }

    private void actionChangeModel(LLModel model) {
        infoText.setText("Model: " + model.getTitle());
        chatModel = modelService.createStreamingChatModel(model);

        chatMemory = TokenWindowChatMemory.withMaxTokens(1000, modelService.createTokenizer(model));
        chatModelName = model.getTitle();
    }

}
