package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MedecinHealPassive extends PassiveAbility {

    @Var(name = "Medic Heal Radius", desc = "Healing zone radius.", type = VariableType.DOUBLE)
    private double healRadius = 6.0;

    @Var(name = "Regeneration Level", desc = "Ally Regeneration level.", type = VariableType.INTEGER)
    private int healLevel = 1;

    public MedecinHealPassive() { setCooldown(0); }

    @Override public String getName() { return "Zone de Soin"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (healLevel <= 0) return false;
        UHCPlayer owner = UHCPlayerManager.get().getPlayer(player);
        if (owner == null || !owner.getTeam().isPresent()) return false;

        boolean healed = false;
        for (UHCPlayer t : owner.getTeam().get().getPlayers()) {
            if (t.equals(owner)) continue;
            Player tp = t.getPlayer();
            if (tp == null || !tp.isOnline()) continue;
            if (tp.getLocation().distance(player.getLocation()) <= healRadius) {
                tp.removePotionEffect(PotionEffectType.REGENERATION);
                tp.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, healLevel - 1, false, false));
                healed = true;
            }
        }
        return healed;
    }
}

