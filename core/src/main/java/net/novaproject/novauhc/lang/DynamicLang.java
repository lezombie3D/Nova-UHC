package net.novaproject.novauhc.lang;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DynamicLang implements Lang {

    private static final Map<String, DynamicLang> REGISTRY = new LinkedHashMap<>();
    private static final List<DynamicLang> PENDING = new ArrayList<>();

    private final String key;
    private final Map<String, String> translations;

    private DynamicLang(String key, String frText) {
        this.key = key;
        this.translations = Map.of("fr_FR", frText);
    }

    public static synchronized DynamicLang of(String key, String frText) {
        DynamicLang existing = REGISTRY.get(key);
        if (existing != null) return existing;
        DynamicLang lang = new DynamicLang(key, frText == null ? "" : frText);
        REGISTRY.put(key, lang);
        PENDING.add(lang);
        return lang;
    }

    public static synchronized List<DynamicLang> drainPending() {
        List<DynamicLang> out = new ArrayList<>(PENDING);
        PENDING.clear();
        return out;
    }

    @Override
    public String name() {
        return key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Map<String, String> getTranslations() {
        return translations;
    }
}

