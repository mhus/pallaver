package de.mhus.pallaver.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleCalculator {

    @Tool("Calculates the length of a string")
    int stringLength(String s) {
        LOGGER.info("Called stringLength() with s='{}'", s);
        return s.length();
    }

    @Tool("Calculates the sum of two numbers")
    int add(int a, int b) {
        LOGGER.info("Called add() with a={}, b={}", a, b);
        return a + b;
    }

    @Tool("Calculates the square root of a number")
    double sqrt(int x) {
        LOGGER.info("Called sqrt() with x={}", x);
        return Math.sqrt(x);
    }
}
