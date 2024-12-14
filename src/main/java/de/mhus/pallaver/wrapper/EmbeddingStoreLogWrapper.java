package de.mhus.pallaver.wrapper;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class EmbeddingStoreLogWrapper<T> implements EmbeddingStore<T> {
    private final EmbeddingStore<T> embeddingStore;

    public EmbeddingStoreLogWrapper(EmbeddingStore<T> embeddingStore) {
        this.embeddingStore = embeddingStore;
    }

    @Override
    public String add(Embedding embedding) {
        return embeddingStore.add(embedding);
    }

    @Override
    public void add(String s, Embedding embedding) {
        embeddingStore.add(s, embedding);
    }

    @Override
    public String add(Embedding embedding, T t) {
        return embeddingStore.add(embedding, t);
    }

    @Override
    public List<String> addAll(List<Embedding> list) {
        return embeddingStore.addAll(list);
    }

    @Override
    public List<String> addAll(List<Embedding> list, List<T> list1) {
        return embeddingStore.addAll(list, list1);
    }

    @Override
    public EmbeddingSearchResult<T> search(EmbeddingSearchRequest request) {
        LOGGER.info("Searching in {} for: {}", embeddingStore.getClass().getSimpleName(), request);
        var result = embeddingStore.search(request);
        LOGGER.info("Found in embedding: {}", result);
        return result;
    }

}
