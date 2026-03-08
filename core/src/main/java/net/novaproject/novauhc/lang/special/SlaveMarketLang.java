package net.novaproject.novauhc.lang.special;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum SlaveMarketLang implements Lang {

    SLAVE_MARKET_NAME("Slave Market", "Slave Market"),
    SLAVE_MARKET_DESC(
            "Système d'enchères où les joueurs peuvent être achetés par d'autres équipes.",
            "Auction system where players can be bought by other teams."
    ),

    UI_TEAM_CONFIG_TITLE("Configuration de l'équipe %team%", "Team %team% configuration"),
    UI_DIAMONDS_LABEL("Diamants : ", "Diamonds: "),
    UI_OWNER_LABEL("Owner : ", "Owner: "),
    UI_NO_OWNER("Aucun owner assigné", "No owner assigned"),
    UI_CLICK_CONFIGURE("Clic pour configurer", "Click to configure"),

    OWNER_ADDED(
            "§a%player% §7a été ajouté comme propriétaire.",
            "§a%player% §7has been added as an owner."
    ),
    OWNER_REMOVED(
            "§c%player% §7a été retiré des propriétaires.",
            "§c%player% §7has been removed from owners."
    ),

    NOT_ENOUGH_OWNERS(
            "§cIl n'y a pas assez de propriétaires pour commencer l'enchère !",
            "§cThere are not enough owners to start the auction!"
    ),
    NOT_ENOUGH_PLAYERS(
            "§cIl n'y a pas assez de joueurs pour commencer l'enchère !",
            "§cThere are not enough players to start the auction!"
    ),

    AUCTION_START(
            "§e%player% §6a été mis en vente ! Enchère de départ : §b0 diamants",
            "§e%player% §6has been put up for auction! Starting bid: §b0 diamonds"
    ),
    AUCTION_TIMER_WARNING(
            "§eIl reste §f%timer% §esecondes pour §a%player% §e! Offre actuelle : §b%bid% diamants%buyer%",
            "§e%timer% §eseconds remaining for §a%player% §e! Current bid: §b%bid% diamonds%buyer%"
    ),
    AUCTION_BUYER_SUFFIX(
            " §epar §6%buyer%",
            " §eby §6%buyer%"
    ),
    AUCTION_SOLD(
            "§a%player% §7a été acheté par §6%buyer% §7pour §b%bid% diamants",
            "§a%player% §7was bought by §6%buyer% §7for §b%bid% diamonds"
    ),
    AUCTION_NOT_SOLD(
            "§a%player% §7n'a pas été acheté et a été assigné à §6%owner%",
            "§a%player% §7was not bought and was assigned to §6%owner%"
    ),
    AUCTION_FINISHED(
            "%servertag%§6Enchère terminée ! En attente de l'host pour le lancement de la partie...",
            "%servertag%§6Auction finished! Waiting for the host to start the game..."
    ),

    BID_PLACED(
            "§6%bidder% §7a enchéri §b%bid% diamants §7pour §a%player%",
            "§6%bidder% §7bid §b%bid% diamonds §7for §a%player%"
    ),
    BID_NOT_ENOUGH_DIAMONDS(
            "§cVous n'avez pas assez de diamants !",
            "§cYou don't have enough diamonds!"
    ),
    BID_ALREADY_HIGHEST(
            "§cVous êtes déjà le plus offrant !",
            "§cYou are already the highest bidder!"
    ),

    UI_TITLE("§6Slave Market", "§6Slave Market"),
    UI_ADD_PLAYER_NAME("§2Ajouter un joueur", "§2Add a player"),
    UI_ADD_PLAYER_ANVIL("Nom du joueur", "Player name"),
    UI_PLAYER_NOT_FOUND("§cJoueur introuvable.", "§cPlayer not found."),
    UI_NO_SLOT_AVAILABLE("§cPlus de place disponible pour un propriétaire.", "§cNo slot available for an owner."),
    UI_DIAMOND_ITEM_NAME("§8┃ §fNombre de §3Diamonds §f: §3§l%value%", "§8┃ §fDiamond Count §f: §3§l%value%"),
    UI_DIAMOND_LORE_MODIFY("  §8┃ §fVous permet de §cmodifier", "  §8┃ §fAllows you to §cmodify"),
    UI_DIAMOND_LORE_COUNT("  §8┃ §fle nombre de §3Diamonds§f.", "  §8┃ §fthe number of §3Diamonds§f."),
    UI_DIAMOND_LORE_OWNER("  §8┃ §fdonnés au %main_color%Owner§f.", "  §8┃ §fgiven to the %main_color%Owner§f."),
    UI_WOOL_START("§8┃ §fDémarrer l' §aEnchère", "§8┃ §fStart the §aAuction"),
    UI_WOOL_CANCEL("§8┃ §fAnnuler l' §cEnchère", "§8┃ §fCancel the §cAuction"),
    UI_AUCTION_CANCELLED_TITLE("§cEnchère annulée...", "§cAuction cancelled..."),

    VAR_NB_DIAMOND_NAME("Diamants initiaux", "Initial Diamonds"),
    VAR_NB_DIAMOND_DESC("Nombre de diamants donnés à chaque propriétaire au début de l'enchère.", "Number of diamonds given to each owner at the start of the auction."),

    VAR_AUCTION_TIMER_NAME("Durée de l'enchère", "Auction Duration"),
    VAR_AUCTION_TIMER_DESC("Temps en secondes avant la fin d'une enchère sans relance.", "Time in seconds before an auction ends without a new bid."),

    VAR_REBUY_TIMER_NAME("Délai après relance", "Rebid Timer"),
    VAR_REBUY_TIMER_DESC("Temps en secondes après une nouvelle enchère avant la fin.", "Time in seconds after a new bid before the auction ends."),

    VAR_BID_SMALL_NAME("Enchère petite", "Small Bid"),
    VAR_BID_SMALL_DESC("Montant de l'enchère avec l'émeraude simple.", "Bid amount with the simple emerald."),

    VAR_BID_LARGE_NAME("Enchère grande", "Large Bid"),
    VAR_BID_LARGE_DESC("Montant de l'enchère avec le bloc d'émeraude.", "Bid amount with the emerald block."),
    UI_CLICK_REMOVE_OWNER(
        "Clic pour retirer l'owner de cette équipe.",
                "Click to remove the owner from this team."
    ),
    UI_CLICK_ASSIGN_OWNER(
        "Clic pour assigner un owner à cette équipe.",
                "Click to assign an owner to this team."
    );
    private final String fr;
    private final String en;

    SlaveMarketLang(String fr, String en) {
        this.fr = fr;
        this.en = en;
    }

    @Override
    public String getKey() {
        return "slavemarket." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return Map.of("fr_FR", fr, "en_EN", en);
    }
}