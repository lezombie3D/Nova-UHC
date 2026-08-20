package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class NainArmorActive extends UseAbility {

    private static final DynamicLang ARMURE_EXPIREE_TITLE =
            DynamicLang.of("legend.nain.notif.expiree_title", "§bArmure temporaire dissipée");
    private static final DynamicLang ARMURE_EXPIREE_BODY =
            DynamicLang.of("legend.nain.notif.expiree_body", "§7Tu n'es plus protégé, rééquipe-toi.");

    @Var(name = "Nain Armor Duration (s)", desc = "Temporary armor duration.", type = VariableType.INTEGER)
    private int armorDuration = 30;

    @Var(name = "Nain Protection Level", desc = "Armor Protection level.", type = VariableType.INTEGER)
    private int protLevel = 2;

    public NainArmorActive() { setCooldown(600); setMaxUse(-1); }

    @Override public String getName() { return "Armure du Nain"; }

    @Override
    public boolean onEnable(Player player) {
        if (protLevel <= 0) return false;
        PlayerInventory inv = player.getInventory();
        inv.setHelmet(piece(Material.DIAMOND_HELMET)); inv.setChestplate(piece(Material.DIAMOND_CHESTPLATE));
        inv.setLeggings(piece(Material.DIAMOND_LEGGINGS)); inv.setBoots(piece(Material.DIAMOND_BOOTS));
        player.updateInventory();
        Bukkit.getScheduler().runTaskLater(Main.get(), () -> removeArmor(player), 20L * armorDuration);
        return true;
    }

    private org.bukkit.inventory.ItemStack piece(Material m) {
        return new ItemCreator(m).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protLevel)
                .setName("§bArmure Temporaire").getItemstack();
    }

    private void removeArmor(Player p) {
        if (p == null || !p.isOnline()) return;
        PlayerInventory inv = p.getInventory();
        boolean removed = false;
        if (isTemp(inv.getHelmet())) { inv.setHelmet(null); removed = true; }
        if (isTemp(inv.getChestplate())) { inv.setChestplate(null); removed = true; }
        if (isTemp(inv.getLeggings())) { inv.setLeggings(null); removed = true; }
        if (isTemp(inv.getBoots())) { inv.setBoots(null); removed = true; }
        p.updateInventory();
        if (removed) {
            DisplayService.notification(p,
                    LangManager.get().get(ARMURE_EXPIREE_TITLE, p),
                    LangManager.get().get(ARMURE_EXPIREE_BODY, p), 4);
        }
    }

    private boolean isTemp(org.bukkit.inventory.ItemStack i) {
        return i != null && i.hasItemMeta() && i.getItemMeta().hasDisplayName()
                && i.getItemMeta().getDisplayName().equals("§bArmure Temporaire");
    }
}

