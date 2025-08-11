package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.solo;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class PereJurasique extends CromagnonRole {
    private UHCPlayer playerRole;

    @Override
    public String getName() {
        return "Pere Jurassique";
    }

    @Override
    public String getDescription() {
        return "§8§m---------" + ChatColor.GOLD + "Le Père Jurassique§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.GOLD + "est des gagner seul.\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + "Vous possedez un effet de speed et de weakness de nuit, vous disposez aussi d'un effet de getForce et de résistance de jour.\n" +
                "§fDescription du role : " + ChatColor.GOLD + "Vous êtes la personne la plus puissante sur le champ de bataille, vous bénéficiez d'une épée Tranchant 5 ainsi que 5 coeurs supplémentaires , Bonne chance.\n" +

                "§8§m--------------------------";
    }

    @Override
    public String getCamps() {
        return "solo1";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public List<Integer> getPowerUse() {
        return Collections.emptyList();
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLDEN_APPLE);
    }


    @Override
    public PotionEffect[] getDayEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false),
                new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false),
                new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false),
        };
    }

    @Override
    public PotionEffect[] getNightEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false),
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false),
        };
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        playerRole = uhcPlayer;
        ItemCreator item = new ItemCreator(Material.ENCHANTED_BOOK).addEnchantment(Enchantment.DAMAGE_ALL, 3);
        uhcPlayer.getPlayer().getWorld().dropItemNaturally(uhcPlayer.getPlayer().getLocation(), item.getItemstack()).setPickupDelay(0);
    }

    @Override
    public boolean isDay() {

        return super.isDay();
    }

    @Override
    public boolean isNight() {

        return super.isNight();
    }
}
