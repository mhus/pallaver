package de.mhus.pallaver.tools;

import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchInformationResult;
import dev.langchain4j.web.search.WebSearchOrganicResult;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DuckDuckGoWebsearchEngine implements WebSearchEngine  {

    private final static String DUCKDUCKGO_SEARCH_URL = "https://html.duckduckgo.com/html/?q=";

    @Override
    public WebSearchResults search(WebSearchRequest webSearchRequest) {
        List<WebSearchOrganicResult> webResults = new ArrayList<>(webSearchRequest.maxResults());
        try {
            var searchRequest = DUCKDUCKGO_SEARCH_URL +
                    URLEncoder.encode(webSearchRequest.searchTerms(), StandardCharsets.UTF_8);
            Document doc = Jsoup.connect(searchRequest)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer(DUCKDUCKGO_SEARCH_URL)
                    .timeout(12000)
                    .followRedirects(true)
                    .ignoreContentType(true)
                    .get();
            LOGGER.info("Searching DuckDuckGo Request: {}\n Result: {}", searchRequest, doc);
            Elements results = doc.getElementById("links").getElementsByClass("results_links");

            for(Element result: results){

                Element title = result.getElementsByClass("links_main").first().getElementsByTag("a").first();
                var url = title.attr("href");
                var text = title.text();
                var snippet = result.getElementsByClass("result__snippet").first().text();
                webResults.add(new WebSearchOrganicResult(text, URI.create(url), snippet, null));

                if (webResults.size() >= webSearchRequest.maxResults())
                    break;
            }
        } catch (IOException e) {
            LOGGER.error("Error while connecting to DuckDuckGo", e);
        }
        return new WebSearchResults(new WebSearchInformationResult((long)webResults.size()), webResults );
    }


}
