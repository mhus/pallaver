package de.mhus.pallaver.chat;

import de.mhus.pallaver.capture.CaptureService;
import de.mhus.pallaver.model.LLModel;
import de.mhus.pallaver.model.ModelService;
import de.mhus.pallaver.wrapper.RetrievalAugmentorLogWrapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSource;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
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
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


@Slf4j
@Service
public class RentalServiceRagFactory implements ChatModelControlFactory {

    @Autowired
    private ModelService modelService;

    @Autowired
    private CaptureService captureService;

    @Value("${tavily.apiKey:}")
    private String tavilyApiKey;

    @Override
    public String getTitle() {
        return "Rental Service";
    }

    @Override
    public ChatModelControl createModelControl(LLModel model, ChatOptions chatOptions, BubbleFactory bubbleFactory) {
        return new WebAccessChatModelControl(model, modelService, chatOptions, captureService, bubbleFactory);
    }

    @Override
    public String getDefaultPrompt() {
        return "Tell me about Miles of Smiles Car Rental Services Terms of Use.";
    }

    private class WebAccessChatModelControl extends ChatModelControl {

        public WebAccessChatModelControl(LLModel model, ModelService modelService, ChatOptions chatOptions, CaptureService captureService, BubbleFactory bubbleFactory) {
            super(model, modelService, chatOptions, captureService, bubbleFactory);
        }

        public void initModel() {
            getChatOptions().setMode(ChatOptions.MODE.CHAT); // force chat mode
            getChatOptions().setUseTools(true);
            super.initModel();
        }

        public ChatAssistant createChatAssistant() {
            EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

            EmbeddingStore<TextSegment> embeddingStore =
                    embed("/documents/miles-of-smiles-terms-of-use.txt", embeddingModel);

            ContentRetriever embeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .maxResults(2)
                    .minScore(0.6)
                    .build();

            // Let's create a query router that will route each query to both retrievers.
            QueryRouter queryRouter = new DefaultQueryRouter(embeddingStoreContentRetriever);

            RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                    .queryRouter(queryRouter)
                    .build();

            return AiServices.builder(ChatAssistant.class)
                    .chatModel(getChatModel())
                    .retrievalAugmentor(new RetrievalAugmentorLogWrapper(retrievalAugmentor))
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .build();
        }

    }

    private EmbeddingStore<TextSegment> embed(String resourcePath, EmbeddingModel embeddingModel) {
        LOGGER.info("Embedding resource: {}", resourcePath);
        DocumentParser documentParser = new TextDocumentParser();
        Document document = loadDocument(getClass().getResourceAsStream(resourcePath), resourcePath, documentParser);

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(document);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);
        return embeddingStore;
    }

    public static Document loadDocument(InputStream is, String resourceName, DocumentParser documentParser) {
            return DocumentLoader.load(new DocumentSource() {
                @Override
                public InputStream inputStream() throws IOException {
                    return is;
                }

                @Override
                public Metadata metadata() {
                    return new Metadata().put("file_name", resourceName);
                }
            }, documentParser);
    }

}
