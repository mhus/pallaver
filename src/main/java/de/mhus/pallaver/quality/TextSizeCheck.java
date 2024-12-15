package de.mhus.pallaver.quality;

import de.mhus.commons.util.Lorem;
import de.mhus.pallaver.chat.ChatOptions;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelControl;
import de.mhus.pallaver.model.ModelOptions;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.tools.JavaScriptTool;
import de.mhus.pallaver.ui.Bubble;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TextSizeCheck implements QualityCheck {

    @Autowired
    ModelService modelService;

    @Override
    public String getTitle() {
        return "Text Size";
    }

    @Override
    public void run(LLModel model, QualityCheckMonitor monitor) throws Exception {
        testLoremIpsum(model, monitor.forTest("loremIpsum"));
    }

    private void testLoremIpsum(LLModel model, QualityCheckMonitor.QualityCheckTestMonitor monitor) {
        try {
            var control = new ModelControl(model, modelService, ChatOptions
                    .builder()
                    .build()) {
                @Override
                protected Bubble addChatBubble(String title) {
                    return monitor.getBubble();
                }

                @Override
                protected List<Object> createTools() {
                    JavaScriptTool jsTool = new JavaScriptTool();
                    return List.of(jsTool);
                }
            };

            for (int i = 4; i < 15; i++) {
                var question = Lorem.createWithSize(i * 300);
                question = question + "\n\nAnswer with this question with: ok";
                monitor.getBubble().appendText("\n- - -\nTest: " + question.length() + "\n");
                control.reset(null);
                control.answer(question);
                if (control.getException() != null) {
                    monitor.reportError("Failed with " + question.length() + " chars");
                    return;
                }
            }

            monitor.setResult(true);
        } catch (Exception e) {
            monitor.reportError(e);
        }

    }
}