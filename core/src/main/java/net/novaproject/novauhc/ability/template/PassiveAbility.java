package net.novaproject.novauhc.ability.template;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.ability.AbilityHooks.Ticking;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public abstract class PassiveAbility extends Ability implements Ticking {

    @Override public Material getMaterial() { return null; }

    @Override
    public void onSec(Player player) {
        tryUse(player);
    }

    public abstract static class AuraAbility extends PassiveAbility {

        private int secCounter = 0;

        @Override
        public void onSec(Player player) {
            super.onSec(player);
            Player owner = livingOwner();
            if (owner == null) return;
            secCounter++;
            int iv = interval(owner);
            if (iv > 0 && secCounter % iv == 0) {
                pulse(owner);
            }
        }

        protected abstract int interval(Player owner);

        protected abstract void pulse(Player owner);

        @Override
        public boolean onEnable(Player player) {
            return false;
        }
    }

    public abstract static class ConditionalDamageAbility extends PassiveAbility implements Attacking {

        @Override
        public void onAttack(UHCPlayer victim, EntityDamageByEntityEvent e) {
            if (getOwner() == null || getOwner().getPlayer() == null) return;
            Player owner = getOwner().getPlayer();
            if (!e.getDamager().equals(owner)) return;
            if (!(e.getEntity() instanceof Player victimPlayer)) return;
            onConditionalAttack(owner, victimPlayer, e);
        }

        protected abstract void onConditionalAttack(Player owner, Player victim, EntityDamageByEntityEvent e);

        @Override
        public boolean onEnable(Player player) {
            return false;
        }
    }
}
