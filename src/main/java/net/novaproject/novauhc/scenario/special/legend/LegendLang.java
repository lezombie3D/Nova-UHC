package net.novaproject.novauhc.scenario.special.legend;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.special.legend.core.LegendData;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum LegendLang implements ScenarioLang {

    // === MESSAGES GÉNÉRAUX ===
    CHOOSE_CLASS_WELCOME,
    CHOOSE_CLASS_TIME_REMAINING,
    CHOOSE_CLASS_TIME_EXPIRED,
    CLASS_ALREADY_CHOSEN,
    CLASS_TAKEN_IN_TEAM,
    CLASS_ASSIGNED_SUCCESS,
    CLASS_ASSIGNED_RANDOM,
    NO_TEAM_ERROR,

    // === MESSAGES DE POUVOIR ===
    POWER_ACTIVATED,
    POWER_COOLDOWN_REMAINING,
    POWER_NO_POWER_AVAILABLE,
    POWER_FAILED,

    // === MESSAGES SPÉCIFIQUES AUX LÉGENDES ===
    MARIONNETTISTE_PUPPET_CREATED,
    MARIONNETTISTE_PUPPET_MASTER_DIED,
    MARIONNETTISTE_PUPPET_RESTRICTION_DROP,
    MARIONNETTISTE_PUPPET_RESTRICTION_COMMAND,
    MARIONNETTISTE_PUPPET_RESTRICTION_POWER,

    MAGE_POTIONS_RECEIVED,

    ARCHER_BOW_CHOICE_PROMPT,
    ARCHER_BOW_CHOICE_INVALID,
    ARCHER_BOW_RECEIVED,

    NAIN_ARMOR_ACTIVATED,
    NAIN_ARMOR_EXPIRED,

    ZEUS_EFFECTS_RECEIVED,

    NECROMANCIEN_NO_ENEMY_FOUND,
    NECROMANCIEN_MOBS_SUMMONED,

    SUCCUBE_ABSORPTION_GIVEN,

    CAVALIER_HORSE_SUMMONED,

    CORNE_MELODY_ACTIVATED,
    CORNE_MELODY_FIRE,
    CORNE_MELODY_HEAL,
    CORNE_MELODY_METAL,
    CORNE_MELODY_AIR,

    PALADIN_BLESSING_ACTIVATED,
    PALADIN_BLESSING_RECEIVED,

    // === MESSAGES D'INTERFACE ===
    UI_CHOOSE_TITLE,
    UI_CHOOSE_INFO_AVAILABLE,
    UI_CHOOSE_INFO_TEAM,
    UI_CHOOSE_CLASS_AVAILABLE,
    UI_CHOOSE_CLASS_TAKEN,
    UI_CHOOSE_CLASS_HAS_POWER,
    UI_CHOOSE_CLASS_SPECIAL_CHOICE,
    UI_CHOOSE_CLICK_TO_SELECT,

    // === MESSAGES D'EFFETS ===
    EFFECT_RECEIVED,
    EFFECT_EXPIRED,

    // === MESSAGES DE MORT ===
    DEATH_LEGEND_DEFEATED,
    DEATH_PUPPET_MASTER_DIED,

    ;

    private static FileConfiguration config;

    @Override
    public String getBasePath() {
        return "messages";
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
            placeholders.put("%team_name%", player.getTeam().get().getName());
            placeholders.put("%team_size%", player.getTeam().get().getPlayers().size());
        } else {
            placeholders.put("%team_name%", "Aucune");
            placeholders.put("%team_size%", 0);
        }

        return placeholders;
    }
}
