package net.novaproject.novauhc.utils;

import java.util.ArrayList;
import java.util.List;

public class BlinkEffect {

    private int count = 0;
    private boolean back = false;
    private final String baseText;
    private final List<String> frames;

    public BlinkEffect(String baseText) {
        this.baseText = baseText;
        this.frames = generateFrames(baseText);
    }

    private List<String> generateFrames(String text) {
        List<String> list = new ArrayList<>();
        int length = text.length();
        if (length > 30) return list;
        for (int i = 0; i < length; i++) {
            StringBuilder sb = new StringBuilder("§6");
            for (int j = 0; j < length; j++) {
                char c = text.charAt(j);
                if (i == j) {
                    sb.append("§e").append(c);
                } else {
                    sb.append("§6").append(c);
                }
            }
            list.add(sb.toString());
        }
        list.add("§6" + text);
        return list;
    }

    public void next() {
        if (count == frames.size() - 1) back = true;
        if (count == 0) back = false;

        if (!back) count++;
        else count--;

    }

    public String getText() {
        return frames.get(count);
    }
}
