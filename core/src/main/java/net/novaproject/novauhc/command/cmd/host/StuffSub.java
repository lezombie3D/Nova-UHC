package net.novaproject.novauhc.command.cmd.host;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lobby.HotbarManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.novaproject.novauhc.player.utils.InventorySnapshots.clearPlayerInventory;
import static net.novaproject.novauhc.player.utils.InventorySnapshots.getInventoryContentsAsString;
import static net.novaproject.novauhc.player.utils.InventorySnapshots.restorePlayerInventory;
import static net.novaproject.novauhc.player.utils.InventorySnapshots.savePlayerInventory;

public class StuffSub extends HostSub {

    private final Set<Player> beingModif = new HashSet<>();

    @Override
    protected void run(Player player, CommandArguments args) {
        if (args.size() < 2) return;
        if (!UHCManager.get().isLobby()) return;

        String type = args.get(0, "");
        String action = args.get(1, "");

        Map<String, ItemStack[]> inventory =
                type.equalsIgnoreCase("death") ? UHCManager.get().death : UHCManager.get().start;

        switch (action) {
            case "clear":
                inventory.clear();
                LangManager.get().send(CoreLang.CMD_STUFF_DELETED, player);
                break;
            case "list":
                LangManager.get().send(CoreLang.CMD_STUFF_LIST, player, Map.of("%content%", getInventoryContentsAsString(inventory, player)));
                break;
            case "modif":
                clearPlayerInventory(player);
                player.setGameMode(GameMode.CREATIVE);
                restorePlayerInventory(player, inventory);
                beingModif.add(player);
                TextComponent base = new TextComponent("Modification du stuff : ");
                TextComponent save = new TextComponent("§a§lSauvegarder");
                TextComponent cancel = new TextComponent(" §fou §c§lAnnuler");
                save.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("§aSauvegarder l'inventaire actuel").create()));
                cancel.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("§cAnnuler la modification").create()));
                String cmd = "/h stuff " + (type.equalsIgnoreCase("death") ? "death " : "start ");
                save.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd + "save"));
                cancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd + "cancel"));
                player.spigot().sendMessage(base, save, cancel);
                break;
            case "save":
                if (!beingModif.contains(player)) {
                    LangManager.get().send(CoreLang.CMD_STUFF_NOT_EDITING, player);
                    return;
                }
                if (type.equalsIgnoreCase("death")) {
                    UHCManager.get().death = savePlayerInventory(player);
                } else {
                    UHCManager.get().start = savePlayerInventory(player);
                }
                player.setGameMode(GameMode.ADVENTURE);
                clearPlayerInventory(player);
                HotbarManager.get().giveHotbar(player);
                LangManager.get().send(CoreLang.CMD_STUFF_SAVED, player);
                beingModif.remove(player);
                break;
            case "cancel":
                beingModif.remove(player);
                player.setGameMode(GameMode.ADVENTURE);
                clearPlayerInventory(player);
                HotbarManager.get().giveHotbar(player);
                break;
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() == 1) return getStrings(args, "death", "start");
        if (args.size() == 2) return getStrings(args, "clear", "list", "modif", "save", "cancel");
        return List.of();
    }
}

