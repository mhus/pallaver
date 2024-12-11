package de.mhus.pallaver.quality;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.ui.Bubble;
import de.mhus.pallaver.ui.ChatOptions;
import de.mhus.pallaver.ui.ModelControl;
import de.mhus.pallaver.ui.ModelOptions;
import de.mhus.pallaver.ui.QualityCheckMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CalculateSimpleCheck implements QualityCheck {

    @Autowired
    ModelService modelService;

    @Override
    public String getTitle() {
        return "Calculate Simple";
    }

    @Override
    public void run(LLModel model, QualityCheckMonitor monitor) throws Exception {

        var control = new ModelControl(model, modelService, ChatOptions
                .builder()
                .mode(ChatOptions.MODE.CHAT)
                .maxMessages(10)
                .maxTokens(1000)
                .useTools(false)
                .modelOptions(new ModelOptions())
                .build()) {
            @Override
            protected Bubble addChatBubble(String title) {
                return monitor;
            }

            @Override
            protected List<Object> createTools() {
                return List.of();
            }
        };

        var answer = control.answer("Answer with one single Number. What is 18*9+2*55?").get();

        var text = answer.text();

        monitor.setResult(text.trim().equals("272"));
    }
}
