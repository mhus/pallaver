package de.mhus.pallaver.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Slf4j
public class WebRequestTool {


    @Tool("You can use this tool to get download a file from a URL")
    public String executeJavaScriptCode(
            @P("URL")
            String url
    ) {
        LOGGER.info("Downloading file from: {}", url);
        if (url == null || url.isBlank()) {
            return "URL is empty";
        }
        if (url.contains("://localhost") || url.contains("://127.")) {
            return "URL contains localhost";
        }
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .timeout(12000)
                    .followRedirects(true)
                    .ignoreContentType(true)
                    .get();
            var result = doc.body().text();
            LOGGER.info("Downloaded file: {}", result);
            return result;
        } catch (Exception e) {
            LOGGER.error("Error while downloading file", e);
            return e.toString();
        }
    }


    public static void main(String[] args) {
        String url = "https://jsoup.org/cookbook/extracting-data/attributes-text-html";
        System.out.println(new WebRequestTool().executeJavaScriptCode(url));
    }
}
