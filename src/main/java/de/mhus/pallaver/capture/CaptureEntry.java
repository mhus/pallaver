package de.mhus.pallaver.capture;

import java.util.Date;

public record CaptureEntry(Date date, String type, String text) {
    public CaptureEntry(String type, String text) {
        this(new Date(), type, text);
    }
}
