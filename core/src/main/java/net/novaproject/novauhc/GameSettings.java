package net.novaproject.novauhc;

import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeamManager;
import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.display.apollo.VisualFeedback;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableDescriptor;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.variable.Variables;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class GameSettings {

    private static final String APOLLO_KEY = "__apollo__";

    @Var(category = "partie", name = "Slots", desc = "Nombre de places dans la partie.", min = 1)
    private int slot = 25;
    @Var(category = "partie", name = "Taille des équipes", desc = "1 = solo, 2+ = mode équipe.", min = 1)
    private int team_size = 1;
    @Var(category = "minage", name = "Limite diamant (armure)", desc = "Pièces d'armure en diamant maximum.", min = 0)
    private int diamondArmor = 2;
    @Var(category = "minage", name = "Protection max", desc = "Niveau maximum de l'enchantement Protection.", min = 0)
    private int protectionMax = 2;
    @Var(category = "minage", name = "Limite de diamants", desc = "Diamants minables par joueur.", min = 0)
    private int diamondLimit = 22;
    @Var(category = "combat", name = "Invulnérabilité", desc = "Durée d'invulnérabilité au début (secondes).", type = VariableType.TIME, min = 0)
    private int invulnerabilityDuration = 30;
    @Var(category = "combat", name = "Timer PvP", desc = "Activation du PvP (secondes).", type = VariableType.TIME, min = 0)
    private int pvpTimer = 60 * 20;
    @Var(category = "border", name = "Timer border", desc = "Activation de la border (secondes).", type = VariableType.TIME, min = 0)
    private int borderTimer = 3600;
    @Var(category = "border", name = "Taille finale", desc = "Taille finale de la border.", min = 1)
    private double targetSize = 200.0;
    @Var(category = "border", name = "Vitesse de réduction", desc = "Blocs par seconde de la réduction de border.", min = 1)
    private long reducSpeed = 2;
    @Var(category = "border", name = "Dégâts border", desc = "Dégâts par seconde hors border.", min = 0)
    private double borderDamageAmount = 0.2;
    @Var(category = "border", name = "Marge border", desc = "Distance de tolérance avant les dégâts de border.", min = 0)
    private double borderDamageBuffer = 0.0;
    @Var(category = "monde", name = "Durée d'épisode", desc = "Durée d'un épisode (secondes, 0 = désactivé).", type = VariableType.TIME, min = 0)
    private int episodeDuration = 0;
    @Var(category = "monde", name = "Cycle jour/nuit custom", desc = "Active le cycle jour/nuit personnalisé.")
    private boolean customDayCycle = false;
    @Var(category = "monde", name = "Durée du jour", desc = "Durée du jour custom (secondes).", type = VariableType.TIME, min = 1)
    private int dayDuration = 600;
    @Var(category = "monde", name = "Durée de la nuit", desc = "Durée de la nuit custom (secondes).", type = VariableType.TIME, min = 1)
    private int nightDuration = 600;
    @Var(category = "monde", name = "Annonce jour/nuit", desc = "Annonce les transitions jour/nuit.")
    private boolean announceDayNight = false;
    @Var(category = "minage", name = "Boost diamant", desc = "Bonus d'apparition du diamant.", type = VariableType.PERCENTAGE, min = 0)
    private double boost_Diamond = 0;
    @Var(category = "minage", name = "Boost or", desc = "Bonus d'apparition de l'or.", type = VariableType.PERCENTAGE, min = 0)
    private double boost_Gold = 0;
    @Var(category = "minage", name = "Boost fer", desc = "Bonus d'apparition du fer.", type = VariableType.PERCENTAGE, min = 0)
    private double boost_Iron = 0;
    @Var(category = "minage", name = "Boost lapis", desc = "Bonus d'apparition du lapis.", type = VariableType.PERCENTAGE, min = 0)
    private double boost_Lapis = 0;
    @Var(category = "minage", name = "Multiplicateur de caves", desc = "Densité des caves (1-5).", min = 1, max = 5)
    private int caveMultiplier = 2;
    @Var(category = "minage", name = "Multiplicateur de ravins", desc = "Densité des ravins (1-5).", min = 1, max = 5)
    private int ravineMultiplier = 2;
    @Var(category = "divers", name = "Chat désactivé", desc = "Bloque le chat global des joueurs.")
    private boolean chatDisabled = false;
    @Var(category = "combat", name = "Force globale", desc = "Pondération des dégâts Force (nerf combat).", type = VariableType.PERCENTAGE, min = 0)
    private double globalForcePercent = 0.5;
    @Var(category = "combat", name = "Résistance globale", desc = "Pondération de la Résistance (nerf combat).", type = VariableType.PERCENTAGE, min = 0)
    private double globalResistancePercent = 1.0;
    @Var(category = "combat", name = "Critique global", desc = "Pondération des coups critiques (nerf combat).", type = VariableType.PERCENTAGE, min = 0)
    private double globalForceCriticPercent = 1.0;
    @Var(category = "combat", name = "Vitesse globale", desc = "Pondération de l'effet Vitesse.", type = VariableType.PERCENTAGE, min = 0)
    private double globalSpeedPercent = 0.20;
    @Var(category = "divers", name = "Pourcentage de vie visible", desc = "Affiche la vie des joueurs en pourcentage.")
    private boolean showHealthPercent = false;
    @Var(category = "divers", name = "Scoreboard des kills", desc = "Affiche le classement des kills sur le scoreboard.")
    private boolean showKillScoreboard = false;
    @Var(category = "divers", name = "Pseudos vocaux anonymes", desc = "Masque les pseudos Minecraft dans Mumble derrière un jeton.")
    private boolean mumbleScrambleNames = false;
    @Var(category = "divers", name = "Tags d'équipe dans le vocal", desc = "Affiche le tag d'équipe dans le menu de modération vocal.")
    private boolean mumbleShowTeamTags = false;
    @Var(category = "monde", name = "Plateformes de départ", desc = "Active les plateformes de scattering.")
    private boolean startPlatformsEnabled = false;
    @Var(category = "combat", name = "Dégâts d'ender pearl", desc = "Dégâts à l'atterrissage d'une pearl (-1 = vanilla).", min = -1)
    private double pearlDamage = -1;
    @Var(category = "combat", name = "Lava bloquée avant PvP", desc = "Interdit de poser de la lave avant le PvP.")
    private boolean blockLavaBeforePvp = true;
    @Var(category = "divers", name = "Ralentissement de faim", desc = "Chance d'ignorer une perte de faim (0-1).", type = VariableType.PERCENTAGE, min = 0, max = 1)
    private double hungerSlowdown = 0;
    @Var(category = "partie", name = "Mode spectateur", desc = "Autorise les spectateurs.")
    private boolean spectator = false;
    @Var(category = "partie", name = "Durée des votes", desc = "Durée des votes host (secondes).", type = VariableType.TIME, min = 5)
    private int voteDuration = 30;
    @Var(category = "partie", name = "Auto-start", desc = "Lance automatiquement la partie quand le lobby est plein.")
    private boolean autoStartEnabled = false;
    @Var(category = "partie", name = "Joueurs pour l'auto-start", desc = "Nombre de joueurs déclenchant l'auto-start.", min = 2)
    private int autoStartMinPlayers = 20;
    @Var(category = "partie", name = "Décompte de lancement", desc = "Durée du décompte avant le scattering (secondes).", type = VariableType.TIME, min = 0)
    private int startCountdownSeconds = 10;
    @Var(category = "divers", name = "TP auto du spectateur", desc = "Téléporte le mort sur son tueur en spectateur.")
    private boolean spectatorAutoTpKiller = true;
    @Var(category = "divers", name = "Alerte diamants", desc = "Alerte le staff et les /h spec quand un joueur mine des diamants.")
    private boolean alertFoundDiamond = true;
    @Var(category = "border", name = "Taille initiale de border", desc = "Taille de la border au lancement.", min = 100)
    private double borderDefaultSize = 2000.0;
    @Var(category = "divers", name = "Fenêtre d'assist", desc = "Durée (secondes) pendant laquelle un dégât compte comme assist.", type = VariableType.TIME, min = 0)
    private int statsAssistWindow = 30;
    @Var(category = "combat", name = "Tag de combat", desc = "Durée (secondes) du tag combat après un coup.", type = VariableType.TIME, min = 0)
    private int combatTagSeconds = 15;
    @Var(category = "divers", name = "Cooldown chat lobby", desc = "Délai (ms) entre deux messages au lobby. 0 = désactivé.", min = 0)
    private int lobbyChatCooldownMs = 0;
    @Var(category = "divers", name = "Blocage du drop d'items de pouvoir", desc = "Empêche de jeter les items de pouvoir.")
    private boolean abilityItemsBlockDrop = true;
    @Var(category = "divers", name = "Items de pouvoir invisibles", desc = "Cache les items de pouvoir aux autres joueurs.")
    private boolean abilityItemsHideFromOthers = true;
    @Var(category = "combat", name = "Combat log", desc = "PNJ de combat-log à la déconnexion en combat.")
    private boolean combatLogEnabled = true;
    @Var(category = "divers", name = "Infos dans le tab", desc = "Affiche le header/footer d'informations dans le tab.")
    private boolean showTabInfo = true;
    @Var(category = "divers", name = "Notifications modération", desc = "Notifie le staff et les /h spec des pouvoirs, combats, commandes et groupes trop grands. Les joueurs morts ne reçoivent rien.")
    private boolean spectatorNotifications = true;
    @Var(category = "combat", name = "Anti obsi-trap", desc = "Annule les dégâts d'étouffement dans l'obsidienne (anti-trap).")
    private boolean antiObsiTrap = false;
    @Var(category = "monde", name = "Spawns hostiles en surface", desc = "Pourcentage des spawns naturels de monstres en surface (100% = vanilla).", type = VariableType.PERCENTAGE, min = 0, max = 1)
    private double surfaceHostileSpawnPercent = 1.0;
    @Var(category = "monde", name = "Spawns d'animaux", desc = "Pourcentage des apparitions naturelles d'animaux (100% = vanilla).", type = VariableType.PERCENTAGE, min = 0, max = 1)
    private double passiveSpawnPercent = 1.0;
    @Var(category = "monde", name = "Entités interdites", desc = "Types d'entités dont l'apparition naturelle est bloquée, séparés par des virgules.")
    private String disabledEntities = "";
    @Var(category = "monde", name = "Rayon de la bande centrale", desc = "Distance en blocs séparant la bande intérieure de la bande extérieure. 0 désactive les bandes.")
    private int biomeInnerRadius = 0;
    @Var(category = "monde", name = "Bande intérieure : mode", desc = "AUCUN, EXCLURE (les biomes listés sont remplacés) ou SEULEMENT (tout sauf les biomes listés est remplacé).")
    private String biomeInnerMode = "AUCUN";
    @Var(category = "monde", name = "Bande intérieure : biomes", desc = "Biomes concernés par la règle, séparés par des virgules.")
    private String biomeInnerList = "";
    @Var(category = "monde", name = "Bande intérieure : remplacement", desc = "Biome utilisé pour remplacer.")
    private String biomeInnerReplacement = "PLAINS";
    @Var(category = "monde", name = "Bande extérieure : mode", desc = "AUCUN, EXCLURE ou SEULEMENT, au-delà du rayon de la bande centrale.")
    private String biomeOuterMode = "AUCUN";
    @Var(category = "monde", name = "Bande extérieure : biomes", desc = "Biomes concernés par la règle, séparés par des virgules.")
    private String biomeOuterList = "";
    @Var(category = "monde", name = "Bande extérieure : remplacement", desc = "Biome utilisé pour remplacer.")
    private String biomeOuterReplacement = "FOREST";
    @Var(category = "monde", name = "Biomes globaux : mode", desc = "Règle appliquée à toutes les distances, avant les bandes.")
    private String biomeGlobalMode = "AUCUN";
    @Var(category = "monde", name = "Biomes globaux : biomes", desc = "Biomes concernés par la règle globale, séparés par des virgules.")
    private String biomeGlobalList = "";
    @Var(category = "monde", name = "Biomes globaux : remplacement", desc = "Biome utilisé pour remplacer globalement.")
    private String biomeGlobalReplacement = "PLAINS";
    @Var(category = "monde", name = "Mines abandonnées", desc = "Génère les mineshafts dans l'arène.")
    private boolean generateMineshafts = false;
    @Var(category = "monde", name = "Forteresses", desc = "Génère les strongholds dans l'arène.")
    private boolean generateStrongholds = false;
    @Var(category = "monde", name = "Villages", desc = "Génère les villages dans l'arène.")
    private boolean generateVillages = false;
    @Var(category = "monde", name = "Temples", desc = "Génère les temples du désert, de la jungle et les huttes de sorcière.")
    private boolean generateTemples = false;
    @Var(category = "monde", name = "Monuments", desc = "Génère les monuments océaniques.")
    private boolean generateMonuments = false;
    @Var(category = "monde", name = "Donjons", desc = "Génère les donjons souterrains avec spawner.")
    private boolean generateDungeons = true;
    @Var(category = "monde", name = "Lacs d'eau", desc = "Génère les poches d'eau naturelles.")
    private boolean generateWaterLakes = true;
    @Var(category = "monde", name = "Lacs de lave", desc = "Génère les poches de lave naturelles.")
    private boolean generateLavaLakes = true;
    @Var(category = "combat", name = "Vie à la résurrection", desc = "Pourcentage de la vie max rendu quand une mort différée est annulée.", type = VariableType.PERCENTAGE, min = 0, max = 1)
    private double resurrectionHealthPercent = 0.1;
    @Var(category = "combat", name = "Effets affichés", desc = "Aligne le niveau affiché des effets Force/Vitesse/Résistance déjà possédés sur le pourcentage du rôle (Force = 15%/niveau, Vitesse et Résistance = 20%/niveau). Purement visuel : aucun effet n'est accordé et la valeur reste le pourcentage.")
    private boolean mirroredEffects = false;
    @Var(category = "partie", name = "Revive auto jusqu'au PvP", desc = "Ressuscite automatiquement les morts tant que le PvP n'est pas actif.")
    private boolean autoRevivePvp = false;
    @Var(category = "partie", name = "Revive auto jusqu'aux rôles", desc = "Ressuscite automatiquement les morts tant que les rôles ne sont pas distribués.")
    private boolean autoReviveRoles = false;
    @Var(category = "partie", name = "Revive auto jusqu'au Final Heal", desc = "Ressuscite automatiquement les morts tant que le premier Final Heal n'a pas eu lieu.")
    private boolean autoReviveFinalHeal = false;
    @Var(category = "divers", name = "Crafts bloqués", desc = "Matériaux dont le craft est interdit.", type = VariableType.STRING)
    private String blockedCraftMaterials = "";
    @Var(category = "divers", name = "Obtentions bloquées", desc = "Matériaux impossibles à obtenir hors craft.", type = VariableType.STRING)
    private String blockedObtainMaterials = "";

    public void setSlot(int slot) {
        this.slot = Math.max(1, slot);
        if (this.team_size > 1 && UHCTeamManager.get() != null) {
            UHCTeamManager.get().createTeams();
        }
    }

    public void setTeam_size(int team_size) {
        this.team_size = Math.max(1, team_size);
        if (UHCTeamManager.get() != null) {
            if (this.team_size == 1) {
                UHCTeamManager.get().deleteTeams();
            } else {
                UHCTeamManager.get().createTeams();
            }
        }
    }

    public void setDiamondArmor(int diamondArmor) {
        this.diamondArmor = Math.max(0, diamondArmor);
    }

    public void setDiamondLimit(int diamondLimit) {
        this.diamondLimit = Math.max(0, diamondLimit);
        if (UHCPlayerManager.get() != null) {
            UHCPlayerManager.get().getOnlineUHCPlayers()
                    .forEach(p -> p.setDiamondLimit(this.diamondLimit));
        }
    }

    public void setGlobalForcePercent(double globalForcePercent) {
        this.globalForcePercent = globalForcePercent;
        if (UHCPlayerManager.get() != null) {
            UHCPlayerManager.get().getOnlineUHCPlayers()
                    .forEach(p -> p.setForcePercent(globalForcePercent));
        }
    }

    public void setGlobalResistancePercent(double globalResistancePercent) {
        this.globalResistancePercent = globalResistancePercent;
        if (UHCPlayerManager.get() != null) {
            UHCPlayerManager.get().getOnlineUHCPlayers()
                    .forEach(p -> p.setResistancePercent(globalResistancePercent));
        }
    }

    public void setGlobalForceCriticPercent(double globalForceCriticPercent) {
        this.globalForceCriticPercent = globalForceCriticPercent;
        if (UHCPlayerManager.get() != null) {
            UHCPlayerManager.get().getOnlineUHCPlayers()
                    .forEach(p -> p.setForceCriticPercent(globalForceCriticPercent));
        }
    }

    public void setGlobalSpeedPercent(double globalSpeedPercent) {
        this.globalSpeedPercent = globalSpeedPercent;
        if (UHCPlayerManager.get() != null) {
            UHCPlayerManager.get().getOnlineUHCPlayers()
                    .forEach(p -> p.setSpeedPercent(globalSpeedPercent));
        }
    }

    public boolean isEntityDisabled(EntityType type) {
        if (type == null || disabledEntities == null || disabledEntities.isEmpty()) return false;
        for (String name : disabledEntities.split(",")) {
            if (name.equals(type.name())) return true;
        }
        return false;
    }

    public void setEntityDisabled(EntityType type, boolean disabled) {
        if (type == null) return;
        Set<String> names = new LinkedHashSet<>();
        if (disabledEntities != null && !disabledEntities.isEmpty()) {
            Collections.addAll(names, disabledEntities.split(","));
        }
        if (disabled) names.add(type.name());
        else names.remove(type.name());
        disabledEntities = String.join(",", names);
    }

    public void setCaveMultiplier(int v) {
        this.caveMultiplier = Math.max(1, Math.min(5, v));
    }

    public void setRavineMultiplier(int v) {
        this.ravineMultiplier = Math.max(1, Math.min(5, v));
    }

    public Document snapshot() {
        Document doc = new Document();
        for (VariableDescriptor d : Variables.of(this)) {
            try {
                doc.put(d.field().getName(), d.field().get(this));
            } catch (IllegalAccessException ignored) {
            }
        }
        doc.put(APOLLO_KEY, VisualFeedback.ApolloConfig.snapshot());
        return doc;
    }

    public void restore(Document doc) {
        if (doc == null) return;
        if (doc.get(APOLLO_KEY) instanceof Document apollo) {
            VisualFeedback.ApolloConfig.restore(apollo);
        }
        for (VariableDescriptor d : Variables.of(this)) {
            Field field = d.field();
            if (!doc.containsKey(field.getName())) continue;
            try {
                Object value = doc.get(field.getName());
                Class<?> t = field.getType();
                if (value instanceof Number n) {
                    if (t == int.class || t == Integer.class) Variables.write(field, this, n.intValue());
                    else if (t == long.class || t == Long.class) Variables.write(field, this, n.longValue());
                    else if (t == double.class || t == Double.class) Variables.write(field, this, n.doubleValue());
                    else if (t == float.class || t == Float.class) Variables.write(field, this, n.floatValue());
                } else if (value instanceof Boolean b && (t == boolean.class || t == Boolean.class)) {
                    Variables.write(field, this, b);
                } else if (value instanceof String s && t == String.class) {
                    Variables.write(field, this, s);
                }
            } catch (RuntimeException ignored) {
            }
        }
    }

    public boolean isCraftBlocked(Material material) {
        return blockedCraftMaterials.contains("," + material.name() + ",");
    }

    public boolean isObtainBlocked(Material material) {
        return blockedObtainMaterials.contains("," + material.name() + ",");
    }

    public void toggleCraftBlocked(Material material) {
        blockedCraftMaterials = toggled(blockedCraftMaterials, material);
    }

    public void toggleObtainBlocked(Material material) {
        blockedObtainMaterials = toggled(blockedObtainMaterials, material);
    }

    private static String toggled(String list, Material material) {
        String token = "," + material.name() + ",";
        String base = list.isEmpty() ? "," : list;
        return base.contains(token) ? base.replace(token, ",") : base + material.name() + ",";
    }

}

