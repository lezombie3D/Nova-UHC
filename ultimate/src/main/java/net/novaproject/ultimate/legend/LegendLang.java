package net.novaproject.ultimate.legend;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum LegendLang implements Lang {

    CHOOSE_CLASS_WELCOME("§6⚔ §7Legends §r§7Vous avez §f%choose_time% minutes §7pour choisir vos classes : §7/ld choose"),
    CHOOSE_CLASS_TIME_EXPIRED("§6⚔ §7Legends §r§7Le temps de sélection est terminé !"),
    CLASS_ALREADY_CHOSEN("§c§lLegend §8│ §7Vous avez déjà une classe."),
    CLASS_TAKEN_IN_TEAM("§c§lLegend §8│ §7La classe %legend_name% a déjà été choisie dans votre équipe."),
    CLASS_ASSIGNED_SUCCESS("§a§lLegend §8│ §7Vous avez choisi la classe : §f%legend_name%"),
    CLASS_ASSIGNED_RANDOM("§e§lLegend §8│ §7Classe assignée automatiquement : §f%legend_name%"),
    NO_TEAM_ERROR("§c§lLegend §8│ §7Vous devez être dans une équipe."),
    UI_CHOOSE_TITLE("§6§lChoisir une Légende §7(%count% disponibles)"),
    CMD_SCENARIO_NOT_ACTIVE("§c§lLegend §8│ §7Le scénario UHC Legends doit être actif !"),
    CMD_CHOOSE_EXPIRED("§c§lLegend §8│ §7Vous ne pouvez plus choisir de classe."),
    CMD_NO_POWER("§c§lLegend §8│ §7Vous n'avez pas de classe assignée."),

    ROLE_DESC_ASSASSIN("\n  §d§lL'ASSASSIN\n  §7Pouvoirs\n  §8│ §7Lame Secrète (Fer Sharpness I)\n  §8│ §7+%extra_hearts% coeurs supplémentaires\n  §8│ §7Force I si Lame Secrète + < 2 épées\n"),
    ROLE_DESC_MAGE("\n  §d§lLE MAGE\n  §7Pouvoirs\n  §8│ §73 potions jetables au début\n  §8│ §7Régénère 3 potions / 10 minutes\n  §8│ §7(Force, Résistance, Vitesse)\n"),
    ROLE_DESC_ARCHER("\n  §d§lL'ARCHER\n  §7Pouvoirs\n  §8│ §7Arc Power II Infinity I\n  §8│ §7Dégâts d'arc augmentés\n  §8│ §725% chance de Slowness sur flèche\n"),
    ROLE_DESC_TANK("\n  §d§lLE TANK\n  §7Pouvoirs\n  §8│ §7+%extra_hearts% coeurs supplémentaires\n  §8│ §7Résistance I quand vie faible\n"),
    ROLE_DESC_NAIN("\n  §d§lLE NAIN\n  §7Pouvoirs\n  §8│ §7Actif : Armure diamant Prot II (30s)\n  §8│ §7Cooldown : 10 minutes\n  §8│ §7Haste I permanente\n"),
    ROLE_DESC_ZEUS("\n  §d§lZEUS\n  §7Pouvoirs\n  §8│ §710% éclair au corps-à-corps\n  §8│ §720% Speed I 10s au corps-à-corps\n  §8│ §7Actif : 2 effets aléatoires (cd 10min)\n"),
    ROLE_DESC_NECROMANCIEN("\n  §d§lLE NÉCROMANCIEN\n  §7Pouvoirs\n  §8│ §7Actif : 2 squelettes + 3 baby zombies\n  §8│ §7Ciblent l'ennemi le plus proche (cd 10min)\n  §8│ §7Night Vision permanente\n"),
    ROLE_DESC_SUCCUBE("\n  §d§lLA SUCCUBE\n  §7Pouvoirs\n  §8│ §7Vol de vie au corps-à-corps (+1 ♥)\n  §8│ §7Actif : Absorption III équipe (cd 6min)\n"),
    ROLE_DESC_SOLDAT("\n  §d§lLE SOLDAT\n  §7Pouvoirs\n  §8│ §7Résistance I permanente\n  §8│ §7Bonus dégâts épée (corps-à-corps)\n  §8│ §7Équipement fer Prot I complet\n"),
    ROLE_DESC_PRINCESSE("\n  §d§lLA PRINCESSE\n  §7Pouvoirs\n  §8│ §7Immunité dégâts de chute\n  §8│ §7+%extra_hearts% coeurs supplémentaires\n  §8│ §7Speed I permanente\n"),
    ROLE_DESC_CAVALIER("\n  §d§lLE CAVALIER\n  §7Pouvoirs\n  §8│ §7Actif : Cheval Royal (cd 5min)\n  §8│ §7Lance Royale (Diamant Sharp I)\n"),
    ROLE_DESC_OGRE("\n  §d§lL'OGRE\n  §7Pouvoirs\n  §8│ §7+%extra_hearts% coeurs supplémentaires\n  §8│ §f%start_gapples% gapples au début\n  §8│ §7Malus si < 5 gapples (Slow + Weakness)\n  §8│ §7Effet aléatoire en mangeant une gapple\n"),
    ROLE_DESC_DRAGON("\n  §d§lLE DRAGON\n  §7Pouvoirs\n  §8│ §7Fire Resistance permanente\n  §8│ §730% chance feu au corps-à-corps\n  §8│ §7Actif : Boule de feu (cd 5min)\n  §8│ §7+%extra_hearts% coeurs supplémentaires\n"),
    ROLE_DESC_MEDECIN("\n  §d§lLE MÉDECIN\n  §7Pouvoirs\n  §8│ §7Zone Régénération I (6 blocs) alliés\n  §8│ §75 gapples au début\n  §8│ §7Régénération I permanente\n"),
    ROLE_DESC_PRISONNIER("\n  §d§lLE PRISONNIER\n  §7Pouvoirs\n  §8│ §7Speed I permanente\n  §8│ §7Actif : Enchaîne ennemi proche\n  §8│ §7(Slowness II + Weakness I, cd 5min)\n"),
    ROLE_DESC_CORNE("\n  §d§lLA CORNE\n  §7Pouvoirs\n  §8│ §7Weakness I permanente (malus)\n  §8│ §74 Mélodies (buff équipe 30 blocs) :\n§e  ♪ Feu : Fire Res II 12s (cd 1min)\n§a  ♪ Heal : Full heal (cd 10min)\n§9  ♪ Metal : Résist II 5s (cd 1min)\n§b  ♪ Air : Speed II 8s (cd 3min)\n"),
    ROLE_DESC_MARIONNETTISTE("\n  §d§lLE MARIONNETTISTE\n  §7Pouvoirs\n  §8│ §7Kill = victime devient marionnette\n  §8│ §7Rejoint votre équipe (type aléatoire) :\n§c  → Féroce (Force I)\n§9  → Massif (Résistance I)\n§b  → Timide (Speed I)\n  §8│ §7Poison si > 16 blocs\n  §8│ §7Marionnettes meurent si vous mourez\n"),
    ROLE_DESC_PALADIN("\n  §d§lLE PALADIN\n  §7Pouvoirs\n  §8│ §7+%extra_hearts% coeurs supplémentaires\n  §8│ §7Résistance I quand vie faible\n  §8│ §7Force I si allié proche (8 blocs)\n  §8│ §7Actif : Bénédiction (cd 8min)\n  §8│ §7Épée Sacrée (Or Sharp II)\n"),

    ;

    private final Map<String, String> translations;

    LegendLang(String fr) {
        this.translations = Map.of("fr_FR", fr);
    }

    @Override public String getKey() { return "legend." + name(); }
    @Override public Map<String, String> getTranslations() { return translations; }
}

