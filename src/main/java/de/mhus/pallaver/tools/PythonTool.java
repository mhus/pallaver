package de.mhus.pallaver.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;

@Slf4j
public class PythonTool {

    private boolean available;

    public PythonTool() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            available = exitCode == 0;
            if (!available) {
                LOGGER.warn("Python3 is not available");
            }
        } catch (Exception e) {
            LOGGER.warn("Python3 is not available", e);
            available = false;
        }
    }

    @Tool("MUST be used for accurate calculations: math, sorting, filtering, aggregating, string processing, etc")
    public String executePythonCode(
            @P("Python code to execute, result MUST be printed to console")
            String pythonCode
    ) {
        if (!available) {
            return "Error: Python3 is not available";
        }
        LOGGER.info("Executing Python code: {}", pythonCode);
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "-c", pythonCode);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringWriter output = new StringWriter();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.write(line);
                    output.write("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "Error (exit code " + exitCode + "): " + output.toString();
            }
            return output.toString();
        } catch (Exception ex) {
            LOGGER.error("Error executing Python code", ex);
            return "Error: " + ex.toString();
        }
    }
}