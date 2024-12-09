package de.mhus.pallaver.ui;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.code.judge0.Judge0JavaScriptExecutionTool;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class MyJudge0JavaScriptExecutionTool extends Judge0JavaScriptExecutionTool {
    public MyJudge0JavaScriptExecutionTool(String apiKey) {
        super(apiKey);
    }

    public MyJudge0JavaScriptExecutionTool(String apiKey, boolean fixCodeIfNeeded, Duration timeout) {
        super(apiKey, fixCodeIfNeeded, timeout);
    }

    @Tool("MUST be used for accurate calculations: math, sorting, filtering, aggregating, string processing, etc")
    public String executeJavaScriptCode(
            @P("JavaScript code to execute, result MUST be printed to console")
            String javaScriptCode
    ) {
        LOGGER.info("Executing JavaScript code: {}", javaScriptCode);
        return super.executeJavaScriptCode(javaScriptCode);
    }
}
