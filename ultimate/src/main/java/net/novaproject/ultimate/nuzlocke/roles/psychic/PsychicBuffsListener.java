package net.novaproject.ultimate.nuzlocke.roles.psychic;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PsychicBuffsListener extends Ability implements Attacking {

    @Var(name = "Nuzlocke Psychic Bow Mult", type = VariableType.DOUBLE)
    private double bowDealtMult = 1.15;

    @Var(name = "Nuzlocke Psychic Proj Mult", type = VariableType.DOUBLE)
    private double projectileTakenMult = 0.90;

    @Var(name = "Nuzlocke Psychic Melee Taken", type = VariableType.DOUBLE)
    private double meleeTakenMult = 0.90;

    @Var(name = "Nuzlocke Psychic Melee Dealt", type = VariableType.DOUBLE)
    private double meleeDealtMult = 1.35;

    @Override public String getName() { return "Esprit Aiguisé"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onAttack(UHCPlayer victim, EntityDamageByEntityEvent event) {
        if (getOwner() == null) return;
        Player owner = getOwner().getPlayer();
        if (owner == null) return;

        boolean ownerIsDamager = event.getDamager().equals(owner)
                || (event.getDamager() instanceof Arrow a && a.getShooter() instanceof Player s && s.equals(owner));
        boolean ownerIsVictim = event.getEntity().equals(owner);

        if (ownerIsDamager) {
            if (event.getDamager() instanceof Arrow) {
                event.setDamage(event.getDamage() * bowDealtMult);
            } else if (event.getDamager() instanceof Player) {
                event.setDamage(event.getDamage() * meleeDealtMult);
            }
        }
        if (ownerIsVictim) {
            if (event.getDamager() instanceof Arrow) {
                event.setDamage(event.getDamage() * projectileTakenMult);
            } else if (event.getDamager() instanceof Player) {
                event.setDamage(event.getDamage() * meleeTakenMult);
            }
        }
    }
}

