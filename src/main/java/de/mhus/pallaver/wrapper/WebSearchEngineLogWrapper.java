package de.mhus.pallaver.wrapper;

import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSearchEngineLogWrapper implements WebSearchEngine {
    private final WebSearchEngine webSearchEngine;

    public WebSearchEngineLogWrapper(WebSearchEngine webSearchEngine) {
        this.webSearchEngine = webSearchEngine;
    }

    @Override
    public WebSearchResults search(WebSearchRequest webSearchRequest) {
        LOGGER.info("Searching in {} for: {}", webSearchEngine.getClass().getSimpleName(), webSearchRequest);
        var result = webSearchEngine.search(webSearchRequest);
        if (result != null && result.results() != null) {
            for (var webSearchOrganicResult : result.results()) {
                LOGGER.info("Found in web: {}", webSearchOrganicResult);
            }
        }
        return result;
    }
}
