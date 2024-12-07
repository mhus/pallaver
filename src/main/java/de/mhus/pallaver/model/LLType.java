package de.mhus.pallaver.model;

public interface LLType {

    String getTitle();
    String getName();
    String getUrl();

    default boolean equals(LLType type) {
        return getName().equals(type.getName());
    }

}
