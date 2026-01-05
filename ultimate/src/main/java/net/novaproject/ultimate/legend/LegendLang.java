package net.novaproject.ultimate.legend;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.ultimate.legend.core.LegendData;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum LegendLang implements ScenarioLang {

    // === MESSAGES GÉNÉRAUX ===
    CHOOSE_CLASS_WELCOME("%server_tag% &6Vous avez %choose_time% minutes pour choisir vos classes : &e/ld choose"),
    CHOOSE_CLASS_TIME_REMAINING("&6[Legend] &eTemps restant pour choisir : &c%time%"),
    CHOOSE_CLASS_TIME_EXPIRED("&6[Legend] &cLe temps de sélection des classes est terminé !"),
    CLASS_ALREADY_CHOSEN("&c[Legend] Vous avez déjà une classe"),
    CLASS_TAKEN_IN_TEAM("&c[Legend] La classe %legend_name% a déjà été choisie dans votre équipe"),
    CLASS_ASSIGNED_SUCCESS("&a[Legend] Vous avez choisi la classe : &6%legend_name%"),
    CLASS_ASSIGNED_RANDOM("&e[Legend] Classe assignée automatiquement : &6%legend_name%"),
    NO_TEAM_ERROR("&c[Legend] Vous devez être dans une équipe pour choisir une classe"),

    // === MESSAGES DE POUVOIR ===
    POWER_ACTIVATED("&6[%legend_name%] &aPouvoir activé !"),
    POWER_COOLDOWN_REMAINING("&c[Legend] Veuillez attendre encore : &e%time%"),
    POWER_NO_POWER_AVAILABLE("&c[Legend] Votre classe n'a pas de pouvoir activable"),
    POWER_FAILED("&c[Legend] Impossible d'activer le pouvoir"),

    // === MESSAGES SPÉCIFIQUES AUX LÉGENDES ===
    MARIONNETTISTE_PUPPET_CREATED("&6[Marionnettiste] &aVous avez transformé %player% en marionnette !"),
    MARIONNETTISTE_PUPPET_MASTER_DIED("&c[Marionnettiste] Votre maître est mort ! Vous mourez également."),
    MARIONNETTISTE_PUPPET_RESTRICTION_DROP("&c[Marionnette] Vous ne pouvez pas jeter d'objets !"),
    MARIONNETTISTE_PUPPET_RESTRICTION_COMMAND("&c[Marionnette] Vous ne pouvez pas utiliser les commandes de légende !"),
    MARIONNETTISTE_PUPPET_RESTRICTION_POWER("&c[Marionnette] Vous ne pouvez pas utiliser de pouvoirs !"),

    MAGE_POTIONS_RECEIVED("&6[Mage] &aVous avez reçu de nouvelles potions !"),

    ARCHER_BOW_CHOICE_PROMPT("&6[Archer] &eChoisissez votre type d'arc :"),
    ARCHER_BOW_CHOICE_INVALID("&c[Archer] Choix invalide : veuillez choisir 1 ou 2"),
    ARCHER_BOW_RECEIVED("&6[Archer] &aVous avez reçu l'Arc de Lumière !"),

    NAIN_ARMOR_ACTIVATED("&6[Nain] &aArmure en diamant activée pour %duration% secondes !"),
    NAIN_ARMOR_EXPIRED("&6[Nain] &cVotre armure temporaire a disparu !"),

    ZEUS_EFFECTS_RECEIVED("&6[Zeus] &aVous avez reçu des effets divins !"),

    NECROMANCIEN_NO_ENEMY_FOUND("&c[Nécromancien] Aucun ennemi trouvé à proximité !"),
    NECROMANCIEN_MOBS_SUMMONED("&6[Nécromancien] &aMonstres invoqués contre %target% !"),

    SUCCUBE_ABSORPTION_GIVEN("&6[Succube] &aAbsorption III donnée à votre équipe !"),

    CAVALIER_HORSE_SUMMONED("&6[Cavalier] &aCheval invoqué !"),

    CORNE_MELODY_ACTIVATED("&6[Corne] &aMélodie activée !"),
    CORNE_MELODY_FIRE("&6[Corne] &aMélodie de Feu activée !"),
    CORNE_MELODY_HEAL("&6[Corne] &aMélodie de Soin activée !"),
    CORNE_MELODY_METAL("&6[Corne] &aMélodie de Métal activée !"),
    CORNE_MELODY_AIR("&6[Corne] &aMélodie d'Air activée !"),

    PALADIN_BLESSING_ACTIVATED("&6[Paladin] &aBénédiction activée ! Régénération et résistance pendant %duration% secondes."),
    PALADIN_BLESSING_RECEIVED("&6[Paladin] &aVous bénéficiez de la bénédiction de %player% !"),

    // === MESSAGES D'INTERFACE ===
    UI_CHOOSE_TITLE("&6&lChoisir une Légende &7(%count% disponibles)"),
    UI_CHOOSE_INFO_AVAILABLE("&7Classes disponibles : &a%available%&7/&a%total%"),
    UI_CHOOSE_INFO_TEAM("&7Équipe : &b%team_name%"),
    UI_CHOOSE_CLASS_AVAILABLE("&a✓ Disponible"),
    UI_CHOOSE_CLASS_TAKEN("&c✗ Déjà prise dans votre équipe"),
    UI_CHOOSE_CLASS_HAS_POWER("&b⚡ Possède un pouvoir activable"),
    UI_CHOOSE_CLASS_SPECIAL_CHOICE("&e⚠ Choix spécial requis"),
    UI_CHOOSE_CLICK_TO_SELECT("&eCliquez pour choisir cette classe !"),

    // === MESSAGES D'EFFETS ===
    EFFECT_RECEIVED("&aVous avez reçu l'effet : &b%effect%"),
    EFFECT_EXPIRED("&cL'effet %effect% a expiré"),

    // === MESSAGES DE MORT ===
    DEATH_LEGEND_DEFEATED("&6Le %legend_name% %player% &6a été vaincu !"),
    DEATH_PUPPET_MASTER_DIED("&c[Marionnettiste] Toutes vos marionnettes sont mortes avec vous !"),

    ;

    private static FileConfiguration config;
    private final String defaultMessage;

    LegendLang() {
        this("");
    }

    LegendLang(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getBasePath() {
        return "";
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void setConfig(FileConfiguration config) {
        LegendLang.config = config;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();

        placeholders.put("%choose_time%", getConfig().getInt("choose_time", 3));

        LegendData data = Legend.get().getPlayerData(player);
        if (data != null) {
            placeholders.put("%legend_name%", data.getLegendClass().getName());
            placeholders.put("%legend_description%", data.getLegendClass().getDescription());
            placeholders.put("%legend_id%", data.getLegendClass().getId());
            placeholders.put("%puppet_count%", data.getPuppetCount());
            placeholders.put("%summoned_entities%", data.getSummonedEntities().size());
        } else {
            placeholders.put("%legend_name%", "Aucune");
            placeholders.put("%legend_description%", "");
            placeholders.put("%legend_id%", 0);
            placeholders.put("%puppet_count%", 0);
            placeholders.put("%summoned_entities%", 0);
        }

        if (player.getTeam().isPresent()) {
            placeholders.put("%team_name%", player.getTeam().get().name());
            placeholders.put("%team_size%", player.getTeam().get().getPlayers().size());
        } else {
            placeholders.put("%team_name%", "Aucune");
            placeholders.put("%team_size%", 0);
        }

        return placeholders;
    }
}
