package de.mhus.pallaver.chat;

import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.tools.DuckDuckGoWebsearchEngine;
import de.mhus.pallaver.wrapper.WebSearchEngineLogWrapper;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.web.search.WebSearchEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class DuckDuckGoWebAccessFactory implements ChatModelControlFactory {

    @Autowired
    private ModelService modelService;

    @Override
    public String getTitle() {
        return "DuckDuckGo Web Access";
    }

    @Override
    public ChatModelControl createModelControl(LLModel model, ChatOptions chatOptions, BubbleFactory bubbleFactory) {
        return new WebAccessChatModelControl(model, modelService, chatOptions, bubbleFactory);
    }

    @Override
    public String getDefaultPrompt() {
        return "News: How is the DAX currently doing?";
    }

    private class WebAccessChatModelControl extends ChatModelControl {

        public WebAccessChatModelControl(LLModel model, ModelService modelService, ChatOptions chatOptions, BubbleFactory bubbleFactory) {
            super(model, modelService, chatOptions, bubbleFactory);
        }

        public void initModel() {
            getChatOptions().setMode(ChatOptions.MODE.CHAT); // force chat mode
            getChatOptions().setUseTools(true);
            super.initModel();
        }

        public ChatAssistant createChatAssistant() {
            EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

            // Let's create our web search content retriever.
            WebSearchEngine webSearchEngine = new DuckDuckGoWebsearchEngine();

            ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                    .webSearchEngine(new WebSearchEngineLogWrapper(webSearchEngine))
                    .maxResults(5)
                    .build();

            // Let's create a query router that will route each query to both retrievers.
            QueryRouter queryRouter = new DefaultQueryRouter(webSearchContentRetriever);

            RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                    .queryRouter(queryRouter)
                    .build();

            return AiServices.builder(ChatAssistant.class)
                    .chatLanguageModel(getChatModel())
                    .retrievalAugmentor(retrievalAugmentor)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .build();
        }

    }

}
