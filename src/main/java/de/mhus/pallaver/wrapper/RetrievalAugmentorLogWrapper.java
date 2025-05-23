package de.mhus.pallaver.wrapper;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.query.Metadata;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetrievalAugmentorLogWrapper implements RetrievalAugmentor {
    private final RetrievalAugmentor retrievalAugmentor;

    public RetrievalAugmentorLogWrapper(RetrievalAugmentor retrievalAugmentor) {
        this.retrievalAugmentor = retrievalAugmentor;
    }

    @Override
    public AugmentationResult augment(AugmentationRequest augmentationRequest) {
        var result = retrievalAugmentor.augment(augmentationRequest);
        if (result != null) {
            LOGGER.info("Augmented: {}", result.chatMessage());
            if (result.contents() != null) {
                for (var content : result.contents()) {
                    LOGGER.info("- Content: {}", content);
                }
            }
        }
        return result;
    }

//    @Override
//    public UserMessage augment(UserMessage userMessage, Metadata metadata) {
//        var result = retrievalAugmentor.augment(userMessage, metadata);
//        if (result != null) {
//            LOGGER.info("Augmented: {}", result);
//        }
//        return result;
//    }
}
