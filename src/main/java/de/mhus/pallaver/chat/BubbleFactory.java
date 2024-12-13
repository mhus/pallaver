package de.mhus.pallaver.chat;

import de.mhus.pallaver.ui.Bubble;

public interface BubbleFactory {
    Bubble createBubble(String title);
}
