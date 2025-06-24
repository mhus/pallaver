package de.mhus.pallaver.generator;

import de.mhus.commons.util.Lorem;
import de.mhus.pallaver.chat.ChatOptions;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelControl;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.model.SingleModelControl;
import de.mhus.pallaver.quality.QualityCheck;
import de.mhus.pallaver.quality.QualityCheckMonitor;
import de.mhus.pallaver.tools.JavaScriptTool;
import de.mhus.pallaver.ui.Bubble;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TextSizeGenerator implements Generator {

    @Autowired
    ModelService modelService;

    @Override
    public String getTitle() {
        return "Text Size";
    }

    @Override
    public void run(LLModel model, GeneratorMonitor monitor) {
        try {
            var control = new SingleModelControl(model, modelService, ChatOptions
                    .builder()
                    .build()) {
                @Override
                protected Bubble addChatBubble(String title) {
                    return monitor.createQuestionBubble(title);
                }

                @Override
                protected List<Object> createTools() {
                    JavaScriptTool jsTool = new JavaScriptTool();
                    return List.of(jsTool);
                }
            };

            for (int i = 4; i < 15; i++) {
                control.initModel();
                var question = Lorem.createWithSize(i * 300);
                question = question + "\n\nAnswer with this question with: ok";
                var tokenCnt = control.getTokenizer().estimateTokenCountInMessage(UserMessage.from(question));
                var bubble = monitor.createQuestionBubble("Test: " + question.length() + " chars, " + tokenCnt + " tokens");
                control.reset(null);
                control.answer(question);
                if (control.getException() != null) {
                    bubble.appendText("Failed with " + question.length() + " chars");
                    return;
                }
            }

        } catch (Exception e) {
            monitor.reportError(e);
        }

    }
}
