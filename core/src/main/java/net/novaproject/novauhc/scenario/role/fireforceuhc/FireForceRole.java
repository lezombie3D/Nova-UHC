package net.novaproject.novauhc.scenario.role.fireforceuhc;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.scenario.role.Role;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public abstract class FireForceRole extends Role {


    public boolean istrans() {
        return false;
    }

    public boolean hasFragment() {
        return false;
    }

    public PotionEffect[] getEffects() {
        return new PotionEffect[0];
    }


    public PotionEffect[] getDayEffects() {
        return new PotionEffect[0];
    }

    public PotionEffect[] getNightEffects() {
        return new PotionEffect[0];
    }

    public boolean isNight() {
        long time = Common.get().getArena().getTime();
        return time >= 13000 && time < 23500;
    }

    public boolean isDay() {
        long time = Common.get().getArena().getTime();
        return time >= 23500 || time < 13000;
    }

    public double getTransLevel() {
        return 0;
    }

    public void setTransLevel(double value) {
    }

    public double getForceLevel() {
        return 100;
    }

    public void setForceLevel(double value) {

    }

    public double getMaxForce() {
        return 100;
    }

    public void setMaxForce(int value) {

    }

    public void setCamps(String camps) {

    }

    public void setFragment(boolean value) {

    }

    @Override
    public void onSec(Player p) {
        for (PotionEffect activeEffect : getEffects()) {
            p.removePotionEffect(activeEffect.getType());
        }
        for (PotionEffect effect : getEffects()) {
            effect.apply(p);
        }


        if (isNight()) {
            for (PotionEffect activeEffect : getNightEffects()) {
                p.removePotionEffect(activeEffect.getType());
            }
            for (PotionEffect effect : getNightEffects()) {
                effect.apply(p);
            }
        }
        if (isDay()) {
            for (PotionEffect activeEffect : getDayEffects()) {
                p.removePotionEffect(activeEffect.getType());
            }
            for (PotionEffect effect : getDayEffects()) {
                effect.apply(p);
            }
        }


    }


}
