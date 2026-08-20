package net.novaproject.novauhc.lang.lang;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum MumbleUiLang implements Lang {
    MENU_TITLE("§6§lMumble"),
    JOIN_NAME("§6§lRejoindre le Mumble"),
    JOIN_LORE("§7Reçois ton lien de connexion vocal"),
    MANAGE_NAME("§c§lGérer le vocal"),
    MANAGE_LORE("§7Modération : mute, sourd, déplacer, kick"),
    CLOSED_NAME("§8§lVocal indisponible"),
    CLOSED_LORE("§7Aucune session vocale active pour l'instant."),
    NO_SESSION("§cAucune session vocale active pour le moment."),
    ERROR("§cErreur vocal, réessaie dans un instant."),
    DISABLED("§cLe vocal n'est pas configuré sur ce serveur."),
    CONNECT_HEADER("§b● §b§lVOCAL UHC"),
    CONNECT_DESC("§8┃ §7Ce serveur utilise le vocal Mumble §8(§7un serveur par partie§8)"),
    CONNECT_BUTTON("§8┃ §6▸ §6§lClique ici pour rejoindre le vocal"),
    CONNECT_HOVER("§7Ouvre ton client Mumble et connecte-toi"),
    CONNECT_FOOTER("§8┃"),
    PLAYERS_TITLE("§6§lVocal — Modération"),
    MUTE_ALL("§a§lRendre tout le monde muet (micro)"),
    UNMUTE_ALL("§2§lRétablir les micros"),
    DEAFEN_ALL("§c§lRendre tout le monde sourd"),
    UNDEAFEN_ALL("§4§lRétablir les casques"),
    MOVE_ALL_LOBBY("§b§lRamener tout le monde au Lobby"),
    REFRESH("§e§lRafraîchir"),
    NO_PLAYERS("§7Aucun joueur connecté au vocal."),
    PLAYER_MUTED("§c✖ §f%player%"),
    PLAYER_UNMUTED("§a✔ §f%player%"),
    PLAYER_LORE_MIC("§7Micro : %state%"),
    PLAYER_LORE_DEAF("§7Casque : %state%"),
    PLAYER_LORE_ACTION("§eClique pour (dé)muter ce joueur."),
    STATE_MUTED("§ccoupé"),
    STATE_OK("§aactif"),
    STATE_DEAF("§csourd"),

    // ── Audio positionnel (mod MumbleLink) ──
    STATE_LINKED("§alié"),
    STATE_UNLINKED("§cNON LIÉ"),
    // Pastille accolée au pseudo dans la grille de modération : connecté au vocal ET mod
    // positionnel actif (✔ vert) vs connecté mais pas lié (✖ rouge).
    DOT_LINKED("§a✔"),
    DOT_UNLINKED("§c✖"),
    CONNECT_NICK("§8┃ §7Ton pseudo vocal : §6%nick% §8(§7les pseudos sont anonymisés§8)"),
    ;

    private final Map<String, String> translations;
    MumbleUiLang(String fr) { this.translations = Map.of("fr_FR", fr); }
    @Override public String getKey() { return "ui.mumble." + name(); }
    @Override public Map<String, String> getTranslations() { return translations; }
}
