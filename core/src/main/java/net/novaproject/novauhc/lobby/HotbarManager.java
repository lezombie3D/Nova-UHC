package net.novaproject.novauhc.lobby;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.event.UhcAbilityEvents.UhcGiveHotbarEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class HotbarManager {

    private static final HotbarManager INSTANCE = new HotbarManager();

    private static final int FIRST_SLOT = 0;
    private static final int LAST_SLOT = 8;

    public static HotbarManager get() {
        return INSTANCE;
    }

    public void giveHotbar(Player player) {
        List<HotbarItem> items = new ArrayList<>(defaults());

        UhcGiveHotbarEvent event = new UhcGiveHotbarEvent(player, items);
        Bukkit.getPluginManager().callEvent(event);

        for (HotbarItem item : event.getItems()) {
            if (item == null || !item.getAccess().canAccess(player)) continue;
            if (item.getSlot() < FIRST_SLOT || item.getSlot() > LAST_SLOT) continue;
            ItemStack stack = item.buildItem();
            if (stack != null) {
                player.getInventory().setItem(item.getSlot(), stack);
            }
        }
    }

    private List<HotbarItem> defaults() {
        List<HotbarItem> items = new ArrayList<>();
        items.add(new HotbarItem(0, HotbarItem.Access.ALL, () -> Common.get().getTeamItem().getItemstack()));
        items.add(new HotbarItem(1, HotbarItem.Access.ALL, () -> Common.get().getActiveScenario().getItemstack()));
        items.add(new HotbarItem(4, HotbarItem.Access.HOST, () -> Common.get().getConfigItem().getItemstack()));
        items.add(new HotbarItem(8, HotbarItem.Access.ALL, () -> Common.get().getQuitItem().getItemstack()));
        return items;
    }

    public static class HotbarItem {

        public enum Access {
            PLAYER,
            HOST,
            ALL;

            public boolean canAccess(Player player) {
                return switch (this) {
                    case ALL -> true;
                    case HOST -> RankManager.isStaff(player);
                    case PLAYER -> !RankManager.isStaff(player);
                };
            }
        }

        private final int slot;
        private final Access access;
        private final Supplier<ItemStack> item;

        public HotbarItem(int slot, Access access, Supplier<ItemStack> item) {
            this.slot = slot;
            this.access = access;
            this.item = item;
        }

        public int getSlot() {
            return slot;
        }

        public Access getAccess() {
            return access;
        }

        public ItemStack buildItem() {
            return item.get();
        }
    }
}
