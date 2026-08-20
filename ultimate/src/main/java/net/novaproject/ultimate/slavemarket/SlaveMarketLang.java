package net.novaproject.ultimate.slavemarket;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum SlaveMarketLang implements Lang {

    SLAVE_MARKET_NAME("Slave Market"),
    SLAVE_MARKET_DESC("Système d'enchères où les joueurs peuvent être achetés par d'autres équipes."),

    UI_TEAM_CONFIG_TITLE("Configuration de l'équipe %team%"),
    UI_DIAMONDS_LABEL("Diamants : "),
    UI_OWNER_LABEL("Owner : "),
    UI_NO_OWNER("Aucun owner assigné"),
    UI_CLICK_CONFIGURE("Clic pour configurer"),

    OWNER_ADDED("§a%player% §7a été ajouté comme propriétaire."),
    OWNER_REMOVED("§c%player% §7a été retiré des propriétaires."),

    NOT_ENOUGH_OWNERS("§cIl n'y a pas assez de propriétaires pour commencer l'enchère !"),
    NOT_ENOUGH_PLAYERS("§cIl n'y a pas assez de joueurs pour commencer l'enchère !"),

    AUCTION_START("§e%player% §6a été mis en vente ! Enchère de départ : §b0 diamants"),
    AUCTION_TIMER_WARNING("§eIl reste §f%timer% §esecondes pour §a%player% §e! Offre actuelle : §b%bid% diamants%buyer%"),
    AUCTION_BUYER_SUFFIX(" §epar §6%buyer%"),
    AUCTION_SOLD("§a%player% §7a été acheté par §6%buyer% §7pour §b%bid% diamants"),
    AUCTION_NOT_SOLD("§a%player% §7n'a pas été acheté et a été assigné à §6%owner%"),
    AUCTION_FINISHED("%servertag%§6Enchère terminée ! En attente de l'host pour le lancement de la partie..."),

    BID_PLACED("§6%bidder% §7a enchéri §b%bid% diamants §7pour §a%player%"),
    BID_NOT_ENOUGH_DIAMONDS("§cVous n'avez pas assez de diamants !"),
    BID_ALREADY_HIGHEST("§cVous êtes déjà le plus offrant !"),

    UI_TITLE("§6Slave Market"),
    UI_ADD_PLAYER_NAME("§2Ajouter un joueur"),
    UI_ADD_PLAYER_ANVIL("Nom du joueur"),
    UI_PLAYER_NOT_FOUND("§cJoueur introuvable."),
    UI_NO_SLOT_AVAILABLE("§cPlus de place disponible pour un propriétaire."),
    UI_DIAMOND_ITEM_NAME("§8┃ §fNombre de §3Diamonds §f: §3§l%value%"),
    UI_DIAMOND_LORE_MODIFY("  §8┃ §fVous permet de §cmodifier"),
    UI_DIAMOND_LORE_COUNT("  §8┃ §fle nombre de §3Diamonds§f."),
    UI_DIAMOND_LORE_OWNER("  §8┃ §fdonnés au %main_color%Owner§f."),
    UI_WOOL_START("§8┃ §fDémarrer l' §aEnchère"),
    UI_WOOL_CANCEL("§8┃ §fAnnuler l' §cEnchère"),
    UI_AUCTION_CANCELLED_TITLE("§cEnchère annulée..."),

    VAR_NB_DIAMOND_NAME("Diamants initiaux"),
    VAR_NB_DIAMOND_DESC("Nombre de diamants donnés à chaque propriétaire au début de l'enchère."),

    VAR_AUCTION_TIMER_NAME("Durée de l'enchère"),
    VAR_AUCTION_TIMER_DESC("Temps en secondes avant la fin d'une enchère sans relance."),

    VAR_REBUY_TIMER_NAME("Délai après relance"),
    VAR_REBUY_TIMER_DESC("Temps en secondes après une nouvelle enchère avant la fin."),

    VAR_BID_SMALL_NAME("Enchère petite"),
    VAR_BID_SMALL_DESC("Montant de l'enchère avec l'émeraude simple."),

    VAR_BID_LARGE_NAME("Enchère grande"),
    VAR_BID_LARGE_DESC("Montant de l'enchère avec le bloc d'émeraude."),
    UI_CLICK_REMOVE_OWNER("Clic pour retirer l'owner de cette équipe."),
    UI_CLICK_ASSIGN_OWNER("Clic pour assigner un owner à cette équipe.");
    private final String fr;

    SlaveMarketLang(String fr) {
        this.fr = fr;
    }

    @Override
    public String getKey() {
        return "slavemarket." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return Map.of("fr_FR", fr);
    }
}

