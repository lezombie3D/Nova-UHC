package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Quiver extends Scenario implements Listener {

    @Override
    public Family getFamily() { return Family.COMBAT; }

    private static final Map<UUID, Long> LAST_LIMIT_MESSAGE = new HashMap<>();
    private static final long LIMIT_MESSAGE_THROTTLE_MS = 3000L;

    @Var(name = "Limite de flèches", desc = "Nombre maximum de flèches qu'un joueur peut porter.", type = VariableType.INTEGER, min = 1)
    private int arrowLimit = 16;

    @Override
    public String getName() {
        return "Quiver";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.QUIVER, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BOW);
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupItemEvent event) {
        if (!isActive()) return;
        ItemStack ground = event.getItem().getItemStack();
        if (ground.getType() != Material.ARROW) return;
        Player player = event.getPlayer();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;
        int carried = countArrows(player);
        if (carried >= arrowLimit) {
            event.setCancelled(true);
            sendLimitMessage(player);
            return;
        }
        int pickupAmount = ground.getAmount();
        int space = arrowLimit - carried;
        if (pickupAmount <= space) return;
        event.setCancelled(true);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(Material.ARROW, space));
        int notAdded = 0;
        for (ItemStack rest : leftover.values()) {
            notAdded += rest.getAmount();
        }
        event.getItem().setItemStack(new ItemStack(Material.ARROW, pickupAmount - space + notAdded));
        sendLimitMessage(player);
    }

    @EventHandler
    public void onArrowCraft(CraftItemEvent event) {
        if (!isActive()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack result = event.getRecipe().getResult();
        if (result == null || result.getType() != Material.ARROW) return;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;
        int crafts = 1;
        if (event.isShiftClick() && event.getInventory() instanceof CraftingInventory crafting) {
            int minIngredient = Integer.MAX_VALUE;
            for (ItemStack ingredient : crafting.getMatrix()) {
                if (ingredient == null || ingredient.getType() == Material.AIR) continue;
                minIngredient = Math.min(minIngredient, ingredient.getAmount());
            }
            if (minIngredient != Integer.MAX_VALUE) crafts = minIngredient;
        }
        if (countArrows(player) + result.getAmount() * crafts <= arrowLimit) return;
        event.setCancelled(true);
        sendLimitMessage(player);
    }

    private int countArrows(Player player) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.ARROW) {
                total += item.getAmount();
            }
        }
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && cursor.getType() == Material.ARROW) {
            total += cursor.getAmount();
        }
        return total;
    }

    private void sendLimitMessage(Player player) {
        long now = System.currentTimeMillis();
        Long last = LAST_LIMIT_MESSAGE.get(player.getUniqueId());
        if (last != null && now - last < LIMIT_MESSAGE_THROTTLE_MS) return;
        LAST_LIMIT_MESSAGE.put(player.getUniqueId(), now);
        LangManager.get().send(CoreLang.COMMON_QUIVER_LIMIT, player, Map.of("%limit%", arrowLimit));
    }

    @Override
    public void onStop() {
        LAST_LIMIT_MESSAGE.clear();
    }
}
