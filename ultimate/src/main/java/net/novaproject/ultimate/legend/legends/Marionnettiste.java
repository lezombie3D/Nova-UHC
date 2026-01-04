package net.novaproject.ultimate.legend.legends;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.ultimate.legend.Legend;
import net.novaproject.ultimate.legend.core.LegendClass;
import net.novaproject.ultimate.legend.core.LegendData;
import net.novaproject.ultimate.legend.utils.LegendItems;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;
import java.util.Random;

/**
 * Légende du Marionnettiste
 * <p>
 * Capacités :
 * - Pas de pouvoir activable
 * - Spécial : Transforme les ennemis tués en marionnettes
 * - Les marionnettes ont des effets selon leur type (Féroce, Massif, Timide)
 *
 * @author NovaProject
 * @version 3.0
 */
public class Marionnettiste extends LegendClass {

    public Marionnettiste() {
        super(0, "Marionnettiste", "Transforme les ennemis tués en marionnettes", Material.FLOWER_POT);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Marionnettiste] §aVous êtes maintenant un Marionnettiste !");
        player.sendMessage("§7Tuez des ennemis pour les transformer en marionnettes");
        player.sendMessage("§7Vos marionnettes vous aideront au combat");
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§c[Marionnettiste] Vous n'avez pas de pouvoir activable !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        LegendData data = Legend.get().getPlayerData(uhcPlayer);

        for (UHCPlayer puppet : data.getPuppets()) {
            Player puppetPlayer = puppet.getPlayer();
            if (puppetPlayer != null && puppetPlayer.isOnline()) {
                puppetPlayer.setHealth(0.0);
                puppetPlayer.setGameMode(GameMode.SPECTATOR);
                puppetPlayer.sendMessage("§c[Marionnettiste] Votre maître est mort ! Vous mourez également.");
            }
        }

        player.sendMessage("§c[Marionnettiste] Toutes vos marionnettes sont mortes avec vous !");
    }


    @Override
    public void onKill(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        createPuppet(uhcPlayer, killer);
    }

    @Override
    public boolean hasPower() {
        return false; // Pas de pouvoir activable
    }


    public void createPuppet(UHCPlayer deadPlayer, UHCPlayer master) {
        Player puppetPlayer = deadPlayer.getPlayer();
        Player masterPlayer = master.getPlayer();

        if (puppetPlayer == null || masterPlayer == null) return;

        puppetPlayer.getInventory().clear();
        puppetPlayer.getInventory().setArmorContents(new org.bukkit.inventory.ItemStack[4]);
        puppetPlayer.setMaxHealth(20);
        puppetPlayer.setHealth(puppetPlayer.getMaxHealth());
        puppetPlayer.setGameMode(GameMode.SURVIVAL);

        LegendData masterData = Legend.get().getPlayerData(master);
        masterData.addPuppet(deadPlayer);

        if (master.getTeam().isPresent()) {
            deadPlayer.forceSetTeam(Optional.of(master.getTeam().get()));
        }

        puppetPlayer.teleport(masterPlayer.getLocation());

        // Messages
        puppetPlayer.sendMessage("§6[Marionnettiste] §aVous avez été transformé en marionnette !");
        masterPlayer.sendMessage("§6[Marionnettiste] §aVous avez transformé " + puppetPlayer.getName() + " en marionnette !");

        org.bukkit.Bukkit.getScheduler().runTaskLater(net.novaproject.novauhc.Main.get(), () -> {
            setupPuppetEquipment(puppetPlayer, masterData);
        }, 5L);
    }

    private void setupPuppetEquipment(Player puppet, LegendData masterData) {
        LegendItems.givePuppetEquipment(puppet);

        Random random = new Random();
        int puppetType = random.nextInt(3); // 0=Féroce, 1=Massif, 2=Timide

        // Stocker le type dans les données du maître
        masterData.setData("puppet_type_" + puppet.getName(), puppetType);

        // Envoyer le type à la marionnette
        String typeName;
        switch (puppetType) {
            case 0:
                typeName = "§cFéroce §7(+Force)";
                break;
            case 1:
                typeName = "§7Massif §7(+Résistance)";
                break;
            case 2:
                typeName = "§bTimide §7(+Vitesse)";
                break;
            default:
                typeName = "§fInconnu";
        }

        puppet.sendMessage("§6[Marionnettiste] §aVous êtes une marionnette " + typeName);
    }


    public void applyPuppetEffects(UHCPlayer puppet, Player puppetPlayer, UHCPlayer master) {
        LegendData masterData = Legend.get().getPlayerData(master);
        Player masterPlayer = master.getPlayer();

        if (masterPlayer != null && puppetPlayer.getLocation().distance(masterPlayer.getLocation()) > 16) {
            PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 80, 0, false, false);
            puppetPlayer.removePotionEffect(PotionEffectType.POISON);
            puppetPlayer.addPotionEffect(poison);
        }

        Integer puppetType = (Integer) masterData.getData("puppet_type_" + puppetPlayer.getName());
        if (puppetType != null) {
            PotionEffect effect;
            switch (puppetType) {
                case 0: // Féroce
                    effect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false);
                    break;
                case 1: // Massif
                    effect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false);
                    break;
                case 2: // Timide
                    effect = new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false);
                    break;
                default:
                    return;
            }

            puppetPlayer.removePotionEffect(effect.getType());
            puppetPlayer.addPotionEffect(effect);
        }
    }
}
