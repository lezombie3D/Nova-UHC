package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcGameEvents.UhcPvpEnableEvent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;


public final class Restrictions {


    public static class NoFall extends Scenario {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "No Fall"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.NO_FALL, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.DIAMOND_BOOTS); }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        }
    }

    public static class FireLess extends Scenario {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "FireLess"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.FIRE_LESS, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.MAGMA_CREAM); }

        @Override
        public void onDamage(Player player, EntityDamageEvent event) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FIRE
                    || cause == EntityDamageEvent.DamageCause.LAVA
                    || cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
                event.setCancelled(true);
            }
        }
    }

    public static class EndLess extends Scenario {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "EndLess"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.NO_END, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENDER_STONE); }

        @Override
        public void onPortal(PlayerPortalEvent event) {
            if (event.getCause() != PlayerPortalEvent.TeleportCause.END_PORTAL) return;
            event.setCancelled(true);
            event.getPlayer().sendMessage(LangManager.get().get(ScenarioLang.NOEND_BLOCKED, event.getPlayer()));
        }
    }

    public static class NetherLess extends Scenario {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "NetherLess"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.NO_NETHER, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.NETHERRACK); }

        @Override
        public void onPortal(PlayerPortalEvent event) {
            if (event.getCause() != PlayerPortalEvent.TeleportCause.NETHER_PORTAL) return;
            if (ScenarioManager.get().isScenarioActive("NetheriBus")) return;
            event.setCancelled(true);
            event.getPlayer().sendMessage(LangManager.get().get(ScenarioLang.NONETHER_BLOCKED, event.getPlayer()));
        }
    }

    public static class HorseLess extends Scenario implements Listener {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "HorseLess"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.NO_HORSE, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.SADDLE); }

        @EventHandler
        public void onCreatureSpawn(CreatureSpawnEvent event) {
            if (!isActive()) return;
            if (event.getEntityType() == EntityType.HORSE) event.setCancelled(true);
        }

        @Override
        public void onPlayerInteractEntity(Player player, PlayerInteractEntityEvent event) {
            if (event.getRightClicked().getType() == EntityType.HORSE) {
                event.setCancelled(true);
                LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, player);
            }
        }
    }

    public static class NoFood extends Scenario {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "NoFood"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.NO_FOOD, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLDEN_CARROT); }

        @Override
        public void noFood(FoodLevelChangeEvent event) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }

    public static class NoNameTag extends Scenario {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "NoNameTag"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.NO_NAME_TAG, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.NAME_TAG); }
    }

    public static class InfiniteEnchanter extends Scenario {

        @Override
        public Family getFamily() { return Family.CRAFT; }

        @Override public String getName() { return "InfiniteEnchanter"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.INFINITE_ENCHANTER, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.ENCHANTMENT_TABLE); }

        @Var(name = "Niveaux offerts", desc = "Niveaux d'expérience donnés au départ.", type = VariableType.INTEGER, min = 0)
        private int levels = 10000;

        @Var(name = "Tables d'enchantement", desc = "Nombre de tables d'enchantement données au départ.", type = VariableType.INTEGER, min = 0)
        private int enchantTables = 64;

        @Var(name = "Enclumes", desc = "Nombre d'enclumes données au départ.", type = VariableType.INTEGER, min = 0)
        private int anvils = 64;

        @Var(name = "Bibliothèques", desc = "Nombre de bibliothèques données au départ.", type = VariableType.INTEGER, min = 0)
        private int bookshelves = 128;

        @Override
        public void onStart(Player player) {
            player.setLevel(levels);
            give(player, Material.ENCHANTMENT_TABLE, enchantTables);
            give(player, Material.ANVIL, anvils);
            give(player, Material.BOOKSHELF, bookshelves);
        }

        private void give(Player player, Material material, int amount) {
            int left = amount;
            while (left > 0) {
                int stack = Math.min(left, material.getMaxStackSize());
                PlayerUtils.giveOrDrop(player, new ItemStack(material, stack));
                left -= stack;
            }
        }
    }

    public static class NoNotchGap extends Scenario {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "No Notch Gap"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.NO_NOTCH_GAP, player);
        }

        @Override
        public ItemCreator getItem() {
            return new ItemCreator(Material.GOLDEN_APPLE).setDurability((short) 1);
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (result.getType() == Material.GOLDEN_APPLE && result.getDurability() == 1) {
                event.setCancelled(true);
            }
        }
    }

    public static class NoWoodenTool extends Scenario {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "NoWoodenTool"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.NO_WOODEN_TOOL, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.WOOD_SPADE); }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            if (result == null) return;
            Material replacement = switch (result.getType()) {
                case WOOD_SPADE -> Material.IRON_SPADE;
                case WOOD_PICKAXE -> Material.IRON_PICKAXE;
                case WOOD_AXE -> Material.IRON_AXE;
                case WOOD_SWORD -> Material.IRON_SWORD;
                default -> null;
            };
            if (replacement == null) return;
            result.setType(replacement);
            event.getInventory().setResult(result);
        }
    }

    public static class Lavaless extends Scenario implements Listener {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "Lavaless"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.LAVALESS, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.LAVA_BUCKET); }

        @EventHandler
        public void onBucketFill(PlayerBucketFillEvent event) {
            if (!isActive()) return;
            Block clicked = event.getBlockClicked();
            if (clicked == null) return;
            Material type = clicked.getType();
            if (type == Material.LAVA || type == Material.STATIONARY_LAVA) {
                event.setCancelled(true);
                LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, event.getPlayer());
            }
        }

        @EventHandler
        public void onBucketEmpty(PlayerBucketEmptyEvent event) {
            if (!isActive()) return;
            if (event.getBucket() == Material.LAVA_BUCKET) {
                event.setCancelled(true);
                LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, event.getPlayer());
            }
        }
    }

    public static class Rodless extends Scenario {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "Rodless"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.RODLESS, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.FISHING_ROD); }

        @Override
        public void onPlayerInteract(Player player, PlayerInteractEvent event) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.FISHING_ROD) {
                event.setCancelled(true);
                LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, player);
            }
        }

        @Override
        public void onCraft(ItemStack result, CraftItemEvent event) {
            ItemStack item = event.getRecipe().getResult();
            if (item.getType() == Material.FISHING_ROD) {
                event.setCancelled(true);
                LangManager.get().send(CoreLang.COMMON_BLOCKED_CRAFT_ITEM, (Player) event.getWhoClicked());
            }
        }
    }

    public static class ChatPvP extends Scenario implements Listener {

        @Override
        public Family getFamily() { return Family.RESTRICTIONS; }

        @Override public String getName() { return "Chat PvP"; }

        @Override
        public String getDescription(Player player) {
            return LangManager.get().get(ScenarioDescLang.CHAT_PVP, player);
        }

        @Override public ItemCreator getItem() { return new ItemCreator(Material.BOOK_AND_QUILL); }

        @EventHandler
        public void onPvp(UhcPvpEnableEvent event) {
            if (!isActive()) return;
            UHCManager.get().setChatDisabled(true);
            LangManager.get().sendAll(CoreLang.COMMON_CHAT_DISABLED);
        }
    }
}
