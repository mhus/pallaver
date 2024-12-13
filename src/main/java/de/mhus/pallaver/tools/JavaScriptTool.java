package de.mhus.pallaver.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.javascript.engine.RhinoScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

@Slf4j
public class JavaScriptTool {

    @Tool("MUST be used for accurate calculations: math, sorting, filtering, aggregating, string processing, etc")
    public String executeJavaScriptCode(
            @P("JavaScript code to execute, result MUST be printed to console")
            String javaScriptCode
    ) {
        LOGGER.info("Executing JavaScript code: {}", javaScriptCode);
        RhinoScriptEngineFactory factory = new RhinoScriptEngineFactory();
        ScriptEngine jsEngine = factory.getScriptEngine();

//        ScriptEngineManager mgr = new ScriptEngineManager();
//        ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
        try {
            return String.valueOf(jsEngine.eval(javaScriptCode));
        } catch (ScriptException ex) {
            LOGGER.error("Error in script", ex);
            return ex.toString();
        }
    }
}
