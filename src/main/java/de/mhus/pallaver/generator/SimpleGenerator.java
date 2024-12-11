package de.mhus.pallaver.generator;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.ui.Bubble;
import de.mhus.pallaver.ui.ChatOptions;
import de.mhus.pallaver.ui.GeneratorMonitor;
import de.mhus.pallaver.ui.ModelControl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SimpleGenerator implements  Generator {

    @Autowired
    ModelService modelService;

    @Override
    public String getTitle() {
        return "Simple";
    }

    @Override
    public void run(LLModel model, GeneratorMonitor monitor) {
      var options = new ChatOptions();
      options.getModelOptions().setTemperature(1);

      var control = new ModelControl(model, modelService, options) {
          @Override
          protected Bubble addChatBubble(String title) {
              return monitor.createResultBubble(title);
          }

          @Override
          protected List<Object> createTools() {
              return List.of();
          }
      };

      var question = "Schreibe einen Text über das Thema 'Energie'";
      monitor.createQuestionBubble("Question").appendText(question);
      var result = control.answer(question);
      var msg = result.getNow(null);
      LOGGER.info("Result: " + msg);

    }
}
