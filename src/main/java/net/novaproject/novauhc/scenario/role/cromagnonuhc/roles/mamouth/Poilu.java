package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.mamouth;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonCamps;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ShortCooldownManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Poilu extends CromagnonRole {
    private PotionEffect[] effect;
    private boolean send = false;

    public Poilu() {
        setCamp(CromagnonCamps.MAMOUT);
    }

    @Override
    public String getName() {
        return "Poilu";
    }

    @Override
    public String getDescription() {
        return "";
    }


    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.POTION);
    }

    @Override
    public PotionEffect[] getNightEffects() {
        return effect;
    }

    @Override
    public void onSec(Player p) {
        if (isNight()) {
            if (ShortCooldownManager.get(p, "invi") == -1) {
                if (!send) {
                    p.getPlayer().sendMessage("tu peux te mettre invi");
                }
                boolean hasArmor = false;
                for (ItemStack armorPiece : p.getInventory().getArmorContents()) {
                    if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                        hasArmor = true;
                        break;
                    }
                }

                if (!hasArmor) {
                    ShortCooldownManager.put(p, "invi", 1000 * 60 * 10);
                    effect = new PotionEffect[]{
                            new PotionEffect(PotionEffectType.INVISIBILITY, 80, 3, false, false), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false),
                    };
                } else {
                    effect = new PotionEffect[]{
                            new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false),
                    };
                }
            } else {
                p.sendMessage("tu peux pas te mettre invi remez l armure");
            }
        }
        if (ShortCooldownManager.get(p, "invi") == -1) {
            send = false;
        }
    }
}
