package net.novaproject.novauhc.listener;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcWorldEvents.UhcItemMaxLevelEvent;
import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.craft.CraftAbilityManager.CraftConsultRegistry;
import net.novaproject.novauhc.ability.craft.CraftableAbility;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.GoldenHead;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.ui.config.Potions;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.*;

import java.util.Map;
import java.util.stream.Collectors;

public class PlayerCraftEvent implements Listener {

    static boolean isGoldenHead(ItemStack item) {
        return item != null
                && item.getType() == Material.GOLDEN_APPLE
                && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName()
                        .equals(LangManager.get().get(ScenarioLang.GOLDENHEAD_ITEM_NAME));
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = event.getRecipe().getResult().clone();

        if (UHCManager.get().settings().isCraftBlocked(item.getType())) {
            LangManager.get().send(CoreLang.COMMON_BLOCKED_CRAFT_ITEM, player);
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        GoldenHead golden = ScenarioManager.get().getScenario(GoldenHead.class);

        if (golden != null && !golden.isActive() && isGoldenHead(item)) {
            LangManager.get().send(CoreLang.COMMON_BLOCKED_CRAFT_ITEM, player);
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onCraft(item, event);
        });

        if (event.isCancelled()) return;

        CraftConsultRegistry.Entry craftEntry = CraftConsultRegistry.getByResult(item);
        if (craftEntry != null) {
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            Role role = uhcPlayer != null ? KPIBuilder.roleOf(uhcPlayer) : null;
            if (!hasAbility(role, craftEntry.abilityName())) {
                LangManager.get().send(CoreLang.COMMON_BLOCKED_CRAFT_ITEM, player);
                event.setCancelled(true);
                player.updateInventory();
                return;
            }
            CraftableAbility craftable = findCraftable(role, craftEntry);
            if (craftable != null) craftable.onCrafted(player, event);
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void EnchantItemEvent(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (player == null || uhcPlayer == null || event.getItem() == null)
            return;

        if (!containsBlockedEnchant(uhcPlayer, event.getEnchantsToAdd())
                && !exceedsDiamondProtection(uhcPlayer, event.getItem(),
                        event.getEnchantsToAdd().getOrDefault(Enchantment.PROTECTION_ENVIRONMENTAL, 0))) {
            return;
        }

        UhcItemMaxLevelEvent maxLevelEvent =
                new UhcItemMaxLevelEvent(player, event.getEnchantsToAdd());
        Bukkit.getPluginManager().callEvent(maxLevelEvent);
        if (maxLevelEvent.isCancelled()) return;

        event.setCancelled(true);
        LangManager.get().send(CoreLang.COMMON_BLOCKED_ENCHANT, player);
    }

    private boolean exceedsDiamondProtection(UHCPlayer uhcPlayer, ItemStack item, int level) {
        return item != null && ItemCreator.isDiamondArmor(item) && level > uhcPlayer.getProtectionMax();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAnvil(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null)
            return;
        Inventory inv = event.getClickedInventory();
        if (!(inv instanceof AnvilInventory anvil))
            return;
        InventoryView view = event.getView();
        int rawSlot = event.getRawSlot();
        if (rawSlot != view.convertSlot(rawSlot) || rawSlot != 2)
            return;
        ItemStack result = anvil.getItem(2);
        if (result == null || result.getEnchantments() == null)
            return;
        if (exceedsDiamondProtection(uhcPlayer, result,
                result.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL))) {
            event.setCancelled(true);
            LangManager.get().send(CoreLang.COMMON_BLOCKED_ENCHANT, player);
            return;
        }

        if (containsBlockedEnchant(uhcPlayer, result.getEnchantments())) {
            getBlockedEnchant(uhcPlayer, result.getEnchantments()).forEach((enchant, level) -> {
                Enchants ench = Enchants.getEnchant(enchant);
                if (ench != null) uhcPlayer.setEnchantLimit(ench, level);
            });
        }
    }


    @EventHandler
    public void onPrepareFurnaceItem(FurnaceSmeltEvent event) {
        ItemStack item = event.getResult();
        if (item != null && UHCManager.get().settings().isObtainBlocked(item.getType())) {
            event.setCancelled(true);
            return;
        }
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onFurnace(item, event);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onObtainPickup(PlayerPickupItemEvent event) {
        if (UHCManager.get().settings().isObtainBlocked(event.getItem().getItemStack().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onObtainContainerClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (CustomInventory.cache.containsKey(player.getUniqueId())) return;
        Inventory clicked = event.getClickedInventory();
        if (clicked == null || clicked.equals(player.getInventory())) return;
        if (clicked.getType() == InventoryType.CRAFTING || clicked.getType() == InventoryType.PLAYER) return;
        ItemStack moved = event.getCurrentItem();
        if (moved == null) return;
        if (UHCManager.get().settings().isObtainBlocked(moved.getType())) {
            event.setCancelled(true);
            LangManager.get().send(CoreLang.COMMON_BLOCKED_CRAFT_ITEM, player);
        }
    }

    @EventHandler
    public void onBrew(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!(event.getInventory() instanceof BrewerInventory))
            return;
        int slot = event.getRawSlot();
        if (slot == 3) {
            ItemStack result = event.getInventory().getItem(slot);
            if (result == null)
                return;
            for (Potions potion : Potions.values()) {
                if (result.getType() == potion.getMaterial() && !potion.isEnabled()) {
                    event.getInventory().remove(result);
                    event.setCancelled(true);
                    LangManager.get().send(CoreLang.COMMON_BLOCKED_POTION, player);
                    player.closeInventory();
                }
            }
        }
    }

    private boolean containsBlockedEnchant(UHCPlayer player, Map<Enchantment, Integer> enchantments) {
        return enchantments.entrySet().stream().anyMatch(x -> isBlockedEnchant(player, x.getKey(), x.getValue()));
    }

    private Map<Enchantment, Integer> getBlockedEnchant(UHCPlayer player, Map<Enchantment, Integer> enchantments) {
        return enchantments.entrySet().stream()
                .filter(x -> isBlockedEnchant(player, x.getKey(), x.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean isBlockedEnchant(UHCPlayer player, Enchantment enchant, int value) {
        Enchants ench = Enchants.getEnchant(enchant);
        return (player.getEnchantLimits().get(ench) < value);
    }


    private boolean hasAbility(Role role, String abilityName) {
        if (role == null || abilityName == null) return false;
        for (Ability ability : role.getAbilities()) {
            if (ability.getName().equals(abilityName)) {
                return true;
            }
        }
        return false;
    }

    private CraftableAbility findCraftable(Role role, CraftConsultRegistry.Entry entry) {
        if (role == null || entry == null) return null;
        for (Ability ability : role.getAbilities()) {
            if (!(ability instanceof CraftableAbility craftable)) continue;
            if (!entry.abilityName().equals(craftable.getCraftPermissionAbilityName())) continue;
            if (!entry.craftId().equals(craftable.getCraftId())) continue;
            return craftable;
        }
        return null;
    }

}