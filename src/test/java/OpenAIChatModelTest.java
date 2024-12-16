import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class OpenAIChatModelTest {

    @Test
    public void testTokenizer() {
        Arrays.stream(OpenAiChatModelName.values()).forEach(model -> {
            System.out.println("Testing model: " + model);
            var tokenizer = new OpenAiTokenizer(model.toString());
            var cnt = tokenizer.estimateTokenCountInText("Hello, World!");
            System.out.println("Estimated token count: " + cnt);
        });
    }


}
