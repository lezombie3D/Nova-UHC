package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GoldenHead extends Scenario {
    private ShapedRecipe recipe;

    @Override
    public String getName() {
        return "Golden Head";
    }

    @Override
    public String getDescription() {
        return "Les têtes de joueurs morts peuvent être craftées en pommes dorées spéciales.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLDEN_APPLE);
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        ShapedRecipe goldenHead = new ShapedRecipe(new ItemCreator(Material.GOLDEN_APPLE).setName(ChatColor.GOLD + "Golden Head").getItemstack());
        goldenHead.shape("GGG", "GHG", "GGG");
        goldenHead.setIngredient('G', Material.GOLD_INGOT);
        goldenHead.setIngredient('H', new MaterialData(Material.SKULL_ITEM, (byte) 3));
        recipe = goldenHead;
        if (isActive()) {
            Bukkit.addRecipe(goldenHead);
        }
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        if (item.getType() == Material.GOLDEN_APPLE && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Golden Head")) {
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                if (potionEffect.getType().equals(PotionEffectType.REGENERATION) || potionEffect.getType().equals(PotionEffectType.ABSORPTION)) {
                    player.removePotionEffect(potionEffect.getType());
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60 * 2, 1, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 6, 1, false, true));
            }


        } else if (item.getType() == Material.GOLDEN_APPLE) {
            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                if (potionEffect.getType().equals(PotionEffectType.REGENERATION) || potionEffect.getType().equals(PotionEffectType.ABSORPTION)) {
                    player.removePotionEffect(potionEffect.getType());
                }
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60 * 2, 0, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 4, 1, false, true));
            }
        }
    }


    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(uhcPlayer.getPlayer().getName());
        meta.setDisplayName(CommonString.getMessage(CommonString.GOLDEN_HEAD_NAME.getRawMessage(), uhcPlayer));
        skull.setItemMeta(meta);
        uhcPlayer.getPlayer().getWorld().dropItemNaturally(uhcPlayer.getPlayer().getLocation(), skull);
    }
}
