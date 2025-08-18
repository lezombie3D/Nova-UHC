package net.novaproject.novauhc.scenario.special.legend.legends;

import net.novaproject.novauhc.scenario.special.legend.Legend;
import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.scenario.special.legend.core.LegendData;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendEffects;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendItems;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

/**
 * Légende du Nain
 * <p>
 * Capacités :
 * - Pouvoir : Armure en diamant Protection II pendant 30 secondes (Cooldown 10 minutes)
 * - Pas d'effets passifs
 *
 * @author NovaProject
 * @version 3.0
 */
public class Nain extends LegendClass {

    private static final String ARMOR_COOLDOWN = "armor";
    private static final long ARMOR_COOLDOWN_MS = 10 * 60 * 1000L; // 10 minutes
    private static final int ARMOR_DURATION = 30; // 30 secondes

    public Nain() {
        super(5, "Nain", "Invoque une armure en diamant temporaire", Material.GOLD_PICKAXE);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Nain] §aVous êtes maintenant un Nain !");
        player.sendMessage("§7Pouvoir : Armure en diamant temporaire");
        player.sendMessage("§7Durée : 30 secondes, Cooldown : 10 minutes");

        // Donner l'item de pouvoir
        player.getInventory().addItem(LegendItems.createPowerItem("Nain"));
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        LegendData data = Legend.get().getPlayerData(uhcPlayer);

        // Vérifier le cooldown
        if (!data.isCooldownReady(ARMOR_COOLDOWN)) {
            long remaining = data.getCooldownRemaining(ARMOR_COOLDOWN);
            LegendEffects.sendCooldownMessage(player, formatTime(remaining));
            return false;
        }

        // Activer le pouvoir
        giveTemporaryArmor(player);

        // Démarrer le cooldown
        data.setCooldown(ARMOR_COOLDOWN, ARMOR_COOLDOWN_MS);

        LegendEffects.sendPowerActivatedMessage(player, "Nain");
        player.sendMessage("§6[Nain] §aArmure en diamant activée pour " + ARMOR_DURATION + " secondes !");

        return true;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        // Pas d'effets passifs
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        // Pas d'effet spécial à la mort
    }

    /**
     * Donne l'armure temporaire en diamant
     */
    private void giveTemporaryArmor(Player player) {
        PlayerInventory inv = player.getInventory();

        // Donner l'armure en diamant avec Protection II
        inv.setBoots(new ItemCreator(Material.DIAMOND_BOOTS)
                .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
                .setName("§bArmure Temporaire")
                .addLore("§7Disparaît dans " + ARMOR_DURATION + " secondes")
                .getItemstack());

        inv.setLeggings(new ItemCreator(Material.DIAMOND_LEGGINGS)
                .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
                .setName("§bArmure Temporaire")
                .addLore("§7Disparaît dans " + ARMOR_DURATION + " secondes")
                .getItemstack());

        inv.setChestplate(new ItemCreator(Material.DIAMOND_CHESTPLATE)
                .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
                .setName("§bArmure Temporaire")
                .addLore("§7Disparaît dans " + ARMOR_DURATION + " secondes")
                .getItemstack());

        inv.setHelmet(new ItemCreator(Material.DIAMOND_HELMET)
                .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
                .setName("§bArmure Temporaire")
                .addLore("§7Disparaît dans " + ARMOR_DURATION + " secondes")
                .getItemstack());

        player.updateInventory();

        // Programmer la suppression de l'armure
        org.bukkit.Bukkit.getScheduler().runTaskLater(net.novaproject.novauhc.Main.get(), () -> {
            removeTemporaryArmor(player);
        }, 20L * ARMOR_DURATION);
    }

    /**
     * Retire l'armure temporaire
     */
    private void removeTemporaryArmor(Player player) {
        PlayerInventory inv = player.getInventory();

        // Vérifier et retirer l'armure temporaire
        if (inv.getHelmet() != null && inv.getHelmet().hasItemMeta() &&
                inv.getHelmet().getItemMeta().hasDisplayName() &&
                inv.getHelmet().getItemMeta().getDisplayName().equals("§bArmure Temporaire")) {
            inv.setHelmet(null);
        }

        if (inv.getChestplate() != null && inv.getChestplate().hasItemMeta() &&
                inv.getChestplate().getItemMeta().hasDisplayName() &&
                inv.getChestplate().getItemMeta().getDisplayName().equals("§bArmure Temporaire")) {
            inv.setChestplate(null);
        }

        if (inv.getLeggings() != null && inv.getLeggings().hasItemMeta() &&
                inv.getLeggings().getItemMeta().hasDisplayName() &&
                inv.getLeggings().getItemMeta().getDisplayName().equals("§bArmure Temporaire")) {
            inv.setLeggings(null);
        }

        if (inv.getBoots() != null && inv.getBoots().hasItemMeta() &&
                inv.getBoots().getItemMeta().hasDisplayName() &&
                inv.getBoots().getItemMeta().getDisplayName().equals("§bArmure Temporaire")) {
            inv.setBoots(null);
        }

        player.updateInventory();
        player.sendMessage("§6[Nain] §cVotre armure temporaire a disparu !");
    }

    /**
     * Formate un temps en secondes
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return minutes + "m" + (remainingSeconds > 0 ? remainingSeconds + "s" : "");
    }
}
