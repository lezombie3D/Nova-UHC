package net.novaproject.ultimate.nuzlocke;

import net.novaproject.novauhc.lang.Lang;

import java.util.Map;

public enum NuzlockeLang implements Lang {

    TYPE_ASSIGNED("§6⚡ §7Nuzlocke §r§7Votre type est : %nuzlocke_color%§l%nuzlocke_name%"),
    SCENARIO_DESC("§eChaque joueur reçoit un type Pokémon aléatoire au début du jeu. Chaque type apporte des forces, faiblesses et capacités uniques."),

    ROLE_DESC_NORMAL("\n  §f§lNORMAL\n  §7Forces\n  §8│ §7§a/substitute §f: TP 20 blocs aléatoire + Zombie iron full à votre place (cd 5min)\n  §8│ §7Gapple soigne 2.5 coeurs au lieu de 2\n  §8│ §7Résistance 1 permanente\n  §7Faiblesses\n  §8│ §7Aucune\n  §7Env\n  §8│ §7Aucun nerf\n"),

    ROLE_DESC_GRASS("\n  §a§lGRASS\n  §7Atouts\n  §8│ §7Speed I sur l'herbe et les feuilles\n  §8│ §7§a/nearme §f: joueurs dans 100 blocs (cd 5min)\n  §8│ §7Pommes drop des bûches et 5% des feuilles\n  §7Faiblesses\n  §8│ §7Flèches reçues +10% dégâts\n  §8│ §7Hydratation chaque 5min (sinon Slowness II)\n  §8│ §7Dégâts de feu doublés\n"),

    ROLE_DESC_WATER("\n  §9§lWATER\n  §7Atouts\n  §8│ §7Respiration infinie + Depth Strider II\n  §8│ §7Arrête de brûler en sortant du feu\n  §8│ §7Muddy Water : flèche en eau → 50% slip effect 45s (vitesse + portée flèches victime réduites)\n  §7Pouvoir\n  §8│ §7§bÉponge §f: mur d'eau qui repousse les joueurs proches (cd 5min)\n  §7Faiblesses\n  §8│ §7Slowness I près de la lave\n  §8│ §7Mining Fatigue sur l'herbe et les feuilles\n"),

    ROLE_DESC_ROCK("\n  §7§lROCK\n  §7Atouts\n  §8│ §7Haste IV permanente\n  §8│ §7Resistance I permanente\n  §8│ §7Sturdy : 2 coeurs d'absorption regen toutes les 2 min\n  §7Faiblesses\n  §8│ §7Dégâts de chute doublés\n  §8│ §7Slowness I\n  §7Env\n  §8│ §7À 5 stacks de cobble : tout fondu en stone + pioche réparée + Eff III + Unb III + Silk Touch\n"),

    ROLE_DESC_DARK("\n  §8§lDARK\n  §7Atouts\n  §8│ §7Night Vision permanente\n  §8│ §7Flèches donnent Blindness 1s\n  §8│ §7Speed I + 15% dégâts melee la nuit ou hors lumière du soleil\n  §7Commande\n  §8│ §7§a/sound <player> <key> §f: jouer un son la nuit (cd 1min)\n  §7Faiblesses\n  §8│ §7Weakness I + Slowness I exposé à la lumière du soleil\n"),

    ROLE_DESC_ELECTRIC("\n  §e§lELECTRIC\n  §7Forces\n  §8│ §7Speed 2 permanente\n  §8│ §7Paralysis 20% sur flèche (Slowness 6 + Jump 200 2s)\n  §8│ §7Motor Drive (toggle) : flèches 75% plus rapides\n  §7Faiblesses\n  §8│ §7+10% dégâts subis\n  §8│ §7Mining Fatigue près bedrock/obsidian\n  §7Env\n  §8│ §7Sous la pluie (50 blocs) : creepers chargés, pigs→zombie pigmen, immune creeper\n"),

    ROLE_DESC_STEEL("\n  §7§lSTEEL\n  §7Atouts\n  §8│ §7Resistance I permanente\n  §8│ §7Slowness II permanente (→ Slowness I 1min après avoir miné iron ore)\n  §8│ §720% chance de drop minerai en double\n  §7Faiblesses\n  §8│ §7Dégâts feu/lave +150%\n  §7Pouvoir\n  §8│ §7Aimant : 75% knockback négatif (victime attirée) sur coup melee\n"),

    ROLE_DESC_ICE("\n  §b§lICE\n  §7Atouts\n  §8│ §7Speed I permanent, Speed II près de glace\n  §8│ §7Blizzard : flèche Slowness 15s + 20% enfermer cible dans la neige\n  §7Faiblesses\n  §8│ §7Dégâts melee +15%\n  §7Pouvoir\n  §8│ §7§bGlace §f: toggle — eau sous tes pieds → glace 15s. Sur neige : right-click tire snowballs infinis\n"),

    ROLE_DESC_POISON("\n  §5§lPOISON\n  §7Atouts\n  §8│ §7Immunisé au poison\n  §8│ §7Effet Thorns permanent (même sans armure) : 20% dégâts subis renvoyés\n  §8│ §7Slowness I permanent\n  §8│ §7Casser un spawner → 1 oeuf cave spider + 25% potion poison jetable\n  §7Env\n  §8│ §7Mining Fatigue I près de la terre\n"),

    ROLE_DESC_FAIRY("\n  §d§lFAIRY\n  §7Atouts\n  §8│ §7Weakness I permanent\n  §8│ §7Speed I sous la lumière du soleil\n  §8│ §7Sweet Kiss : flèches 50% Nausea 6s\n  §7Faiblesses\n  §8│ §7Slowness I hors lumière du soleil\n  §7Pouvoir\n  §8│ §7§dEnderWand §f: tire des enderpearls (cd 45s)\n  §7Env\n  §8│ §7Manger fleur : +1 gigot, 5% Force I 1min, 5% +1♥, 10% +0.5♥\n"),

    ROLE_DESC_GHOST("\n  §5§lGHOST\n  §7Atouts\n  §8│ §7Slowness I permanent\n  §8│ §7Invisible sous Y=70\n  §8│ §7Aucun dégât PvE sauf feu/lave\n  §8│ §720% de flèches reçues miss\n  §7Faiblesses\n  §8│ §7Weakness I exposé au soleil\n  §7Env\n  §8│ §7Mining Fatigue I près de la terre\n"),

    ROLE_DESC_PSYCHIC("\n  §d§lPSYCHIC\n  §7Atouts\n  §8│ §7Flèches +15% dégâts\n  §8│ §7Coups d'épée +35% dégâts\n  §8│ §7-10% dégâts subis (flèches + melee)\n  §7Commande\n  §8│ §7§a/mindread <player> §f: voir l'inventaire d'un joueur (cd 5min)\n  §7Env\n  §8│ §7Sur /mindread : si cible dans 100 blocs, elle entend un bruit d'enclume\n"),

    ROLE_DESC_DRAGON("\n  §5§lDRAGON\n  §7Atouts\n  §8│ §7Resistance I permanent\n  §8│ §7Flèches touchent les joueurs à moins de 3 blocs de l'impact\n  §8│ §7Extreme Hills + 9+ coeurs : Resistance III permanent\n  §7Faiblesses\n  §8│ §7Mining Fatigue I sous 5 coeurs\n  §8│ §7Slowness I au-dessus de 5 coeurs\n  §7Env\n  §8│ §7Drops augmentés (mob, silex, fer, or)\n"),

    ROLE_DESC_BUG("\n  §2§lBUG\n  §7Atouts\n  §8│ §7Speed I permanent\n  §8│ §7Speed II 5min après avoir mangé des feuilles\n  §8│ §7Flèches → toiles d'araignée 75% radius 1\n  §7Faiblesses\n  §8│ §7Weakness II permanent\n  §8│ §7Gapples soignent 1.5 ♥\n  §7Env\n  §8│ §7Araignées et cave spiders n'attaquent pas\n"),

    ROLE_DESC_GROUND("\n  §6§lGROUND\n  §7Atouts\n  §8│ §7Slowness I permanent\n  §8│ §7Aucuns dégâts de chute\n  §8│ §7Flèches : empêchent de sauter 2s dans 3 blocs (Flying immune)\n  §7Pouvoir\n  §8│ §7§6Pelle §f: tire snowball → sphère dirt 2 blocs (cd 20s) ; à 10+ stacks dirt : sphère plus grande, cd 10s\n  §7Faiblesses\n  §8│ §7Blindness dans l'eau\n"),

    ROLE_DESC_FIGHTING("\n  §6§lFIGHTING\n  §7Atouts\n  §8│ §7Force I permanent\n  §8│ §7-15% dégâts à l'arc\n  §8│ §7Close Combat (faim < moitié) : épée dégât AOE radius 4\n  §8│ §7Ignore la Résistance des victimes au corps-à-corps\n  §7Pouvoir\n  §8│ §7Detect : pare 30s cumulatives → invincible jusqu'à relâcher. Si tu arrêtes avant 30s, cumul -5s. (cd 5min)\n"),

    ROLE_DESC_FLYING("\n  §b§lFLYING\n  §7Atouts\n  §8│ §7Flèches automatiquement Punch\n  §8│ §7Pas de dégâts de chute\n  §8│ §7Jump Boost VI 1s sur dégât de chute simulé (si pouvoir ON)\n  §7Faiblesses\n  §8│ §7Slowness I sous Y=40\n  §8│ §7+50% knockback subi\n  §7Pouvoir\n  §8│ §7§bPlume §f: toggle Speed I + Jump Boost III permanents\n"),

    ROLE_DESC_FIRE("\n  §c§lFIRE\n  §7Forces\n  §8│ §7Résistance au feu permanente\n  §8│ §7Flèches enflamment le sol (et 15% la cible)\n  §8│ §7Cutclean : minerais auto-fondus, viandes auto-cuites\n  §7Faiblesses\n  §8│ §7Nausée dans l'eau\n  §8│ §750% de chance d'obtenir du charbon en cassant un log\n  §7Env\n  §8│ §7En feu sur de l'herbe : laisse une traînée de feu\n"),

    ;

    private final Map<String, String> translations;

    NuzlockeLang(String fr) {
        this.translations = Map.of("fr_FR", fr);
    }

    @Override
    public String getKey() {
        return "nuzlocke." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return translations;
    }
}

