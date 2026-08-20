package net.novaproject.ultimate.beatthesanta;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum BeatTheSantaLang implements Lang {

    WARNING_LUTIN("§c§lAttention ! Vous devez gagner seul, pas en équipe avec les autres lutins."),

    WARNING_SANTA("§c§lAttention ! Vous devez gagner seul, les lutins essaieront de vous tuer en premier avant de se battre entre eux."),

    WARNING_SANTA_DEATH("§eLe Père Noël est mort ! Les lutins doivent maintenant s'entretuer pour gagner !");

    private final Map<String, String> translations;

    BeatTheSantaLang(String fr) {
        this.translations = Map.of("fr_FR", fr);
    }

    @Override
    public String getKey() {
        return "beatthesanta." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return translations;
    }
}

