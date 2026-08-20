package net.novaproject.ultimate.mysteryteam;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum MysteryTeamLang implements Lang {

    FIND_TEAMMATES("§6⚔ §7Trouvez vos coéquipiers ! Taille: §f%team_size%");

    private final Map<String, String> translations;

    MysteryTeamLang(String fr) {
        this.translations = Map.of("fr_FR", fr);
    }

    @Override
    public String getKey() { return "mysteryteam." + name(); }

    @Override
    public Map<String, String> getTranslations() { return translations; }
}

