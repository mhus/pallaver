package de.mhus.pallaver.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

@Slf4j
public class DuckDuckGoSearchTool {

    private final static String DUCKDUCKGO_SEARCH_URL = "https://html.duckduckgo.com/html/?q=";

    @Tool("You can use this tool to get further short information about a topic")
    public String searchWeb(
            @P("Search query")
            String query
    ) {
        LOGGER.info("Searching DuckDuckGo for: {}", query);
        try {
            Document doc = Jsoup.connect(DUCKDUCKGO_SEARCH_URL + query)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer(DUCKDUCKGO_SEARCH_URL)
                    .timeout(12000)
                    .followRedirects(true)
                    .ignoreContentType(true)
                    .get();
            Elements results = doc.getElementById("links").getElementsByClass("results_links");

            StringBuilder sb = new StringBuilder();
            for(Element result: results){

                Element title = result.getElementsByClass("links_main").first().getElementsByTag("a").first();
                sb.append("URL:").append(title.attr("href")).append("\n");
                sb.append("Title:").append(title.text()).append("\n");
                sb.append("Snippet:").append(result.getElementsByClass("result__snippet").first().text()).append("\n\n");
            }

            LOGGER.info("Search results: {}", sb.toString());
            return sb.toString();
        } catch (IOException e) {
            LOGGER.error("Error while connecting to DuckDuckGo", e);
            return e.toString();
        }

    }

}
