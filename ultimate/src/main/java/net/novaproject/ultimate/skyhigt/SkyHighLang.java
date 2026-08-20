package net.novaproject.ultimate.skyhigt;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;
import java.util.HashMap;

public enum SkyHighLang implements Lang {

    DAMAGE_FIRST_LAYER("§cVous prenez des dégâts %first_damage%, montez au-dessus de la couche %first_level%!"),
    DAMAGE_SECOND_LAYER("§cVous prenez des dégâts %second_damage%, montez au-dessus de la couche %second_level%!"),
    DAMAGE_THIRD_LAYER("§cVous prenez des dégâts %third_damage%, montez au-dessus de la couche %third_level%!"),
    WARNING_SKY_HIGH("§eAttention, vous devez rester en hauteur !");

    private final Map<String,String> translations = new HashMap<>();

    SkyHighLang(String fr) {
        translations.put("fr_FR", fr);
    }

    @Override
    public String getKey() {
        return "skyhigh." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return translations;
    }
}

