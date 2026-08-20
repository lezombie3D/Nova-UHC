package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PaladinLowHealthPassive extends PassiveAbility {

    private static final String FOI_VIGNETTE = "textures/misc/vignette.png";

    private boolean foiVignetteShown;

    @Var(name = "Paladin HP Threshold", desc = "HP below which Resistance activates.", type = VariableType.DOUBLE)
    private double threshold = 10.0;

    @Var(name = "Paladin Ally Radius", desc = "Radius for Strength I.", type = VariableType.DOUBLE)
    private double allyRadius = 8.0;

    public PaladinLowHealthPassive() { setCooldown(0); }

    @Override public String getName() { return "Foi du Paladin"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        boolean applied = false;

        boolean blesse = player.getHealth() <= threshold;
        if (blesse) {
            player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false));
            applied = true;
        }

        if (blesse && !foiVignetteShown) {
            DisplayService.vignette(player, FOI_VIGNETTE, 0.55f);
            foiVignetteShown = true;
        } else if (!blesse && foiVignetteShown) {
            DisplayService.resetVignette(player);
            foiVignetteShown = false;
        }

        UHCPlayer owner = UHCPlayerManager.get().getPlayer(player);
        if (owner != null && owner.getTeam().isPresent()) {
            long nearby = owner.getTeam().get().getPlayers().stream()
                    .filter(t -> !t.equals(owner))
                    .map(UHCPlayer::getPlayer)
                    .filter(tp -> tp != null && tp.isOnline()
                            && tp.getLocation().distance(player.getLocation()) <= allyRadius)
                    .count();
            if (nearby > 0) {
                player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false));
                applied = true;
            }
        }

        return applied;
    }
}

