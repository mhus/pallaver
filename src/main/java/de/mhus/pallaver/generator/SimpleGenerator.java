package de.mhus.pallaver.generator;

import de.mhus.pallaver.capture.CaptureService;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.model.SingleModelControl;
import de.mhus.pallaver.ui.Bubble;
import de.mhus.pallaver.chat.ChatOptions;
import de.mhus.pallaver.model.ModelControl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SimpleGenerator implements  Generator {

    @Autowired
    ModelService modelService;

    @Autowired
    CaptureService captureService;

    @Override
    public String getTitle() {
        return "Simple";
    }

    @Override
    public void run(LLModel model, GeneratorMonitor monitor) {
      var options = new ChatOptions();
      options.getModelOptions().setTemperature(1);

      var control = new SingleModelControl(model, modelService, options, captureService) {
          @Override
          protected Bubble addChatBubble(String title) {
              return monitor.createResultBubble(title);
          }

      };

      var concept = ask(control, monitor,
              "Schreibe ein Konzept für eine mittelalterliche, mystische Geschichte mit Elfen, Zauberern, Zwergen und Menschen.");
      var structure = ask(control, monitor,
              "Welche Kapitel sollte das Buch haben?");


    }

    private String ask(ModelControl control, GeneratorMonitor monitor, String question) {
        monitor.createQuestionBubble("Question").appendText(question);
        var result = control.answer(question);
        LOGGER.info("Result: " + result.text());
        return result.text();
    }
}
