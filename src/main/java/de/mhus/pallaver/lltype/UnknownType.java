package de.mhus.pallaver.lltype;

import de.mhus.pallaver.model.LLType;

public class UnknownType implements LLType {
    private final String value;

    public UnknownType(String value) {
        this.value = value;
    }

    @Override
    public String getTitle() {
        return "??? " + value;
    }

    @Override
    public String getName() {
        return value;
    }

    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
