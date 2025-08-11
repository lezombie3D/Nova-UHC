package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.zoms;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.LongCooldownManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

public class Feuille extends CromagnonRole {
    private PotionEffect[] effect;
    private UHCPlayer playerRole;

    @Override
    public String getName() {
        return "Feuille";
    }

    @Override
    public String getDescription() {
        return "§8§m---------" + ChatColor.GREEN + "La Feuille§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.GREEN + "est des gagner avec les autres Zoms.\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + "Vous possedez un effet de speed sous les 5 coeurs ainsi qu'un effet de weakness permanent .\n" +
                "§fDescription du role : " + ChatColor.GREEN + "Vous êtes un Zoms rapide qui possède une nether star vous permettent de dash de 30 blocs vers l'avant avec un cooldown de 3 minutes .\n" +
                "§8§m--------------------------";
    }

    @Override
    public String getCamps() {
        return "zoms";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GREEN;
    }

    @Override
    public List<Integer> getPowerUse() {
        return Collections.emptyList();
    }

    @Override
    public void onSec(Player p) {
        if (p.getHealth() < 11) {
            effect = new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false),
                    new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false)
            };
        } else {
            effect = new PotionEffect[]{
                    new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false)
            };
        }
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        playerRole = uhcPlayer;
        ItemCreator star = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.GREEN + "Dash");
        uhcPlayer.getPlayer().getWorld().dropItemNaturally(uhcPlayer.getPlayer().getLocation(), star.getItemstack()).setPickupDelay(0);

    }

    @Override
    public PotionEffect[] getEffects() {
        return effect;
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.LEAVES);
    }

    @Override
    public void onIteract(Player player, PlayerInteractEvent event) {
        if (player.equals(playerRole.getPlayer())) {
            ItemStack item = player.getItemInHand();

            if (item == null || item.getType() != Material.NETHER_STAR || !item.hasItemMeta()) {
                return;
            }

            if (!item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Dash")) {
                return;
            }

            if (LongCooldownManager.get(player.getUniqueId(), "dash") == -1) {
                LongCooldownManager.put(player.getUniqueId(), "dash", 30 * 1000);
                dashPlayer(player);
            } else {
                player.sendMessage(ChatColor.RED + "Veillez a attendre encore : " + LongCooldownManager.get(player.getUniqueId(), "dash") / 1000 + "s");
            }
        }
    }

    private void dashPlayer(Player player) {
        Vector direction = player.getLocation().getDirection().multiply(5);
        player.setVelocity(direction);
        player.getWorld().playSound(player.getLocation(), Sound.ENDERDRAGON_WINGS, 1.0f, 1.5f);
    }
}
