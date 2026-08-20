package net.novaproject.ultimate.nuzlocke.roles.rock;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Given;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RockCobbleSmeltEnv extends Ability implements Listener, Given {

    @Var(name = "Nuzlocke Rock Cobble Stacks", type = VariableType.INTEGER)
    private int requiredStacks = 5;

    private static final Set<UUID> ROCK_PLAYERS = new HashSet<>();

    private final Set<UUID> alreadyTriggered = new HashSet<>();

    @Override public String getName() { return "Cobble Smelt"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        ROCK_PLAYERS.add(uhcPlayer.getUniqueId());
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (!ROCK_PLAYERS.contains(event.getPlayer().getUniqueId())) return;
        Player p = event.getPlayer();
        if (alreadyTriggered.contains(p.getUniqueId())) return;
        if (event.getItem().getItemStack().getType() != Material.COBBLESTONE) return;

        org.bukkit.Bukkit.getScheduler().runTaskLater(net.novaproject.novauhc.Main.get(), () -> {
            int total = 0;
            PlayerInventory inv = p.getInventory();
            for (ItemStack it : inv.getContents()) {
                if (it != null && it.getType() == Material.COBBLESTONE) total += it.getAmount();
            }
            if (total < requiredStacks * 64) return;
            int toSmelt = total;
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack it = inv.getItem(i);
                if (it != null && it.getType() == Material.COBBLESTONE) {
                    inv.setItem(i, null);
                }
            }
            inv.addItem(new ItemStack(Material.STONE, toSmelt));
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack it = inv.getItem(i);
                if (it == null) continue;
                Material t = it.getType();
                if (t == Material.WOOD_PICKAXE || t == Material.STONE_PICKAXE
                        || t == Material.IRON_PICKAXE || t == Material.GOLD_PICKAXE
                        || t == Material.DIAMOND_PICKAXE) {
                    it.setDurability((short) 0);
                    it.addUnsafeEnchantment(Enchantment.DIG_SPEED, 3);
                    it.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
                    it.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
                }
            }
            alreadyTriggered.add(p.getUniqueId());
            p.sendMessage("§7§lRock §8│ §7La terre vous récompense : §bcobble smelt, pioche améliorée.");
        }, 1L);
    }
}

