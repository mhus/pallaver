import de.mhus.pallaver.tools.JavaScriptTool;
import org.junit.jupiter.api.Test;

public class JavaScriptToolTest {

    @Test
    public void testHelloWorld() {
        var tool = new JavaScriptTool();
        var result = tool.executeJavaScriptCode("console.log('Hello, World!');");
        System.out.println(result);
    }

}
