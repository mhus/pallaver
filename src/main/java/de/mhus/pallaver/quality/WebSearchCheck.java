package de.mhus.pallaver.quality;

import de.mhus.pallaver.capture.CaptureService;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.model.SingleModelControl;
import de.mhus.pallaver.ui.Bubble;
import de.mhus.pallaver.chat.ChatAssistant;
import de.mhus.pallaver.chat.ChatOptions;
import de.mhus.pallaver.model.ModelControl;
import de.mhus.pallaver.model.ModelOptions;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static org.apache.commons.configuration2.convert.PropertyConverter.toPath;

@Service
@Slf4j
public class WebSearchCheck implements QualityCheck {

    @Autowired
    ModelService modelService;

    @Autowired
    CaptureService captureService;

    @Override
    public String getTitle() {
        return "Web Search";
    }

    @Override
    public void run(LLModel model, QualityCheckMonitor monitor) throws Exception {
        testSimpleSearch(model, monitor.forTest("simpleSearch"));
    }

    private void testSimpleSearch(LLModel model, QualityCheckMonitor.QualityCheckTestMonitor monitor) {
        try {
            var control = new SingleModelControl(model, modelService, ChatOptions
                    .builder()
                    .mode(ChatOptions.MODE.CHAT)
                    .maxMessages(10)
                    .maxTokens(1000)
                    .useTools(true)
                    .modelOptions(new ModelOptions())
                    .build(), captureService) {
                @Override
                protected Bubble addChatBubble(String title) {
                    return monitor.getBubble();
                }

            };

            EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

            EmbeddingStore<TextSegment> embeddingStore =
                    embed(toPath("documents/miles-of-smiles-terms-of-use.txt"), embeddingModel);

            ContentRetriever embeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .maxResults(2)
                    .minScore(0.6)
                    .build();

            // Let's create our web search content retriever.
            WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                    .apiKey(System.getenv("TAVILY_API_KEY")) // get a free key: https://app.tavily.com/sign-in
                    .build();

            ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                    .webSearchEngine(webSearchEngine)
                    .maxResults(3)
                    .build();

            // Let's create a query router that will route each query to both retrievers.
            QueryRouter queryRouter = new DefaultQueryRouter(embeddingStoreContentRetriever, webSearchContentRetriever);

            RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                    .queryRouter(queryRouter)
                    .build();

            AiServices.builder(ChatAssistant.class)
                    .chatModel(control.getChatModel())
                    .retrievalAugmentor(retrievalAugmentor)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .build();



        } catch (Exception e) {
            monitor.reportError(e);
        }
    }

    private static EmbeddingStore<TextSegment> embed(Path documentPath, EmbeddingModel embeddingModel) {
        DocumentParser documentParser = new TextDocumentParser();
        Document document = loadDocument(documentPath, documentParser);

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(document);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);
        return embeddingStore;
    }
}
