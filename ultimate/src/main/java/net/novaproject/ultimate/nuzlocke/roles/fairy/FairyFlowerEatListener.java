package net.novaproject.ultimate.nuzlocke.roles.fairy;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Clicking;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class FairyFlowerEatListener extends Ability implements Clicking {

    @Var(name = "Nuzlocke Fairy Strength Chance", type = VariableType.PERCENTAGE)
    private double strengthChance = 0.05;

    @Var(name = "Nuzlocke Fairy Damage Chance", type = VariableType.PERCENTAGE)
    private double damageChance = 0.05;

    @Var(name = "Nuzlocke Fairy Heal Chance", type = VariableType.PERCENTAGE)
    private double healChance = 0.10;

    @Var(name = "Nuzlocke Fairy Strength Dur", type = VariableType.TIME)
    private int strengthSeconds = 60;

    @Override public String getName() { return "Festin Floral"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onClick(PlayerInteractEvent event, ItemStack item) {
        if (getOwner() == null || !event.getPlayer().equals(getOwner().getPlayer())) return;
        if (item == null || !isFlower(item.getType())) return;
        if (!event.getAction().toString().contains("RIGHT_CLICK")) return;
        Player p = getOwner().getPlayer();
        event.setCancelled(true);

        p.setFoodLevel(Math.min(20, p.getFoodLevel() + 2));
        p.setSaturation(Math.min(p.getSaturation() + 0.5f, p.getFoodLevel()));

        if (item.getAmount() <= 1) p.setItemInHand(null);
        else { item.setAmount(item.getAmount() - 1); p.setItemInHand(item); }

        double r = ThreadLocalRandom.current().nextDouble();
        if (r < strengthChance) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * strengthSeconds, 0, true, false), true);
            p.sendMessage("§d§lFleur §8│ §7Force !");
        } else if (r < strengthChance + damageChance) {
            p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + 2));
            p.sendMessage("§d§lFleur §8│ §7Soin +1 ♥ !");
        } else if (r < strengthChance + damageChance + healChance) {
            p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + 1));
            p.sendMessage("§d§lFleur §8│ §7Soin léger +0.5 ♥ !");
        }
    }

    private boolean isFlower(Material m) {
        return m == Material.RED_ROSE || m == Material.YELLOW_FLOWER || m == Material.DOUBLE_PLANT;
    }
}

