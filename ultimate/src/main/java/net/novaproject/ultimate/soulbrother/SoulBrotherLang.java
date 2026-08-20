package net.novaproject.ultimate.soulbrother;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum SoulBrotherLang implements Lang {

    SOUL_BROTHER_NAME("SoulBrother"),
    SOUL_BROTHER_DESC("Les équipes sont séparées dans 2 mondes identiques, réunies après %minutes% minutes !"),
    SOULS_SEPARATED("§6Les âmes soeurs ont été séparées dans deux mondes !"),
    TEAM_SIZE_MINIMUM("§cSoulBrother nécessite des équipes d'au moins 2 joueurs. Taille forcée à 2."),
    WORLDS_NOT_AVAILABLE("§cLes mondes ne sont pas disponibles !"),
    SOUL_BROTHER_LINKED("§bTon âme soeur est §f%brother%§b !"),
    NO_SOUL_BROTHER("§7Tu n'as pas d'âme soeur pour cette partie."),
    ASSIGNED_TO_WORLD("§7Tu as été assigné au monde §f%world%§7."),
    BROTHER_DIED_BROADCAST("§c%player% §7est mort. Son âme soeur §c%brother% §7subit les conséquences..."),
    BROTHER_DIED("§cTon âme soeur §f%brother% §cest morte ! Tu subis un contrecoup !"),
    REUNION_WARNING_MINUTES("§6La réunion des âmes soeurs aura lieu dans §f%minutes% minutes§6 !"),
    REUNION_WARNING_ONE_MINUTE("§6La réunion des âmes soeurs aura lieu dans §f1 minute§6 !"),
    REUNION_WARNING_SECONDS("§cRéunion dans §f%seconds% secondes§c !"),
    REUNION_BROADCAST("§6§lLes âmes soeurs sont réunies !"),
    REUNION_PERSONAL("§bTu as été téléporté pour la réunion !"),
    REUNION_BROTHER_NAME("§bTon âme soeur §f%brother% §beste avec toi !"),
    REUNION_BONUS("§aBonus de réunion reçu !"),
    SOUL_UPDATE("§7Âme soeur §f%brother% §7— Position: §f%x%§7, §f%z% §7— Vie: §f%health%§7❤"),

    VAR_REUNION_TIME_NAME("Temps de réunion"),
    VAR_REUNION_TIME_DESC("Temps avant la réunion des âmes soeurs (en secondes)."),

    VAR_UPDATE_INTERVAL_NAME("Intervalle d'update"),
    VAR_UPDATE_INTERVAL_DESC("Temps entre chaque update de position envoyé aux joueurs (en secondes)."),

    VAR_DEATH_BACKLASH_NAME("Contrecoup de mort"),
    VAR_DEATH_BACKLASH_DESC("Inflige des dégâts et des effets au frère quand son âme soeur meurt."),

    VAR_DEATH_BACKLASH_DAMAGE_NAME("Dégâts de contrecoup"),
    VAR_DEATH_BACKLASH_DAMAGE_DESC("Dégâts infligés au frère survivant lors du contrecoup."),

    VAR_DEATH_BACKLASH_DURATION_NAME("Durée des effets de contrecoup"),
    VAR_DEATH_BACKLASH_DURATION_DESC("Durée en ticks des effets (lenteur, cécité) infligés lors du contrecoup."),

    VAR_REUNION_SCATTER_RADIUS_NAME("Rayon de dispersion réunion"),
    VAR_REUNION_SCATTER_RADIUS_DESC("Rayon en blocs autour du spawn pour téléporter les joueurs lors de la réunion.");

    private final String fr;

    SoulBrotherLang(String fr) {
        this.fr = fr;
    }

    @Override
    public String getKey() {
        return "soulbrother." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return Map.of("fr_FR", fr);
    }
}

