package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.zoms;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.LongCooldownManager;
import net.novaproject.novauhc.utils.ShortCooldownManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class Feu extends CromagnonRole {
    private UHCPlayer playerRole;
    private boolean enchanted;

    @Override
    public String getName() {

        return "Feu";
    }

    @Override
    public String getDescription() {
        return "§8§m---------" + ChatColor.GREEN + "La Feu§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.GREEN + "est des gagner avec les autres Zoms.\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + "Vous possedez un pouvoir particulier qui enflamme vos armes.\n" +
                "§fDescription du role : " + ChatColor.GREEN + "Vous êtes un Zoms rapide qui possède une nether star vous permettent d'enflammer vôtre épée (fire aspect) et votre arc (flame) pendant 1 minute. L'utilisation de ce pouvoir est utilisable dans un délai de 5 minutes entre chaques utilisations.\n" +
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
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        playerRole = uhcPlayer;
        ItemCreator star = new ItemCreator(Material.NETHER_STAR).setName(ChatColor.RED + "Danse du feu");
        uhcPlayer.getPlayer().getWorld().dropItemNaturally(uhcPlayer.getPlayer().getLocation(), star.getItemstack()).setPickupDelay(0);

    }


    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FIREBALL);
    }

    @Override
    public void onIteract(Player player, PlayerInteractEvent event) {
        if (player.equals(playerRole.getPlayer())) {
            ItemStack item = player.getItemInHand();

            if (item == null || item.getType() != Material.NETHER_STAR || !item.hasItemMeta()) {
                return;
            }

            if (!item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Danse du feu")) {
                return;
            }

            if (LongCooldownManager.get(player.getUniqueId(), "CDFuse") == -1) {
                LongCooldownManager.put(player.getUniqueId(), "CDFuse", 15 * 60 * 1000);
                ShortCooldownManager.put(player, "fire", 1000 * 60);
                addFireAspectToSwords(player);
                enchanted = true;
            } else {
                player.sendMessage(ChatColor.RED + "Veillez a attendre encore : " + LongCooldownManager.get(player.getUniqueId(), "CDFuse") / 1000 + "s");
            }
        }
    }

    @Override
    public void onSec(Player p) {
        if (enchanted && ShortCooldownManager.get(p, "fire") == -1) {
            removeFireAspectFromSwords(p);
            enchanted = false;
        }
    }

    public void addFireAspectToSwords(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().name().endsWith("_SWORD")) {
                item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            }
        }
        player.sendMessage(ChatColor.GOLD + "Toutes vos épées ont reçu Fire Aspect !");
    }

    public void removeFireAspectFromSwords(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().name().endsWith("_SWORD")) {
                item.removeEnchantment(Enchantment.FIRE_ASPECT);
            }
        }
        player.sendMessage(ChatColor.GREEN + "Toutes vos épées ont perdu Fire Aspect !");
    }

}
