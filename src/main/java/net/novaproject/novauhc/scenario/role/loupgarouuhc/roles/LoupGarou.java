package net.novaproject.novauhc.scenario.role.loupgarouuhc.roles;

import net.novaproject.novauhc.scenario.role.loupgarouuhc.LoupGarouRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LoupGarou extends LoupGarouRole {

    @Override
    public String getName() {
        return "Loup Garou";
    }

    @Override
    public String getDescription() {
        return "Vous avez force la nuit";
    }


    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);

    }

    @Override
    public PotionEffect[] getNightEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 0, false, false),
        };
    }



}
