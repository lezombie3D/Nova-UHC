package net.novaproject.novauhc;

import org.bukkit.entity.Player;

public enum CommonString {

    CLICK_HERE_TO_APPLY(" §8» §fCliquez pour §a§lappliquer§f."),
    CLICK_HERE_TO_ACTIVATE(" §8» §fCliquez pour §a§lactiver§f."),
    CLICK_HERE_TO_DESACTIVATE(" §8» §fCliquez pour §c§ldésactiver§f."),
    CLICK_HERE_TO_MODIFY(" §8» §fCliquez pour §6§lmodifier§f."),
    CLICK_HERE_TO_ACCESS(" §8» §fCliquez pour y " + Common.get().getMainColor() + "accéder§f."),
    BAR("§f§m                                                                           §r"),
    NO_PERMISSION("§cI'm sorry but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error."),
    CLICK_GAUCHE(" §8» §fCliquez gauche pour §f"),
    CLICK_DROITE(" §8» §fCliquez droite pour §f");
    private final String message;

    CommonString(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void send(Player player) {
        player.sendMessage(this.message);
    }
}
