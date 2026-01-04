package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.NinjaLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Ninja extends Scenario {

    @Override
    public String getName() {
        return "Ninja";
    }

    @Override
    public String getDescription() {
        return "Devenez invisible pendant 10 secondes après chaque kill !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FEATHER);
    }

    @Override
    public String getPath() {
        return "ninja";
    }

    @Override
    public ScenarioLang[] getLang() {
        return NinjaLang.values();
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        if (killer != null) {
            Player killerPlayer = killer.getPlayer();

            // Give invisibility effect based on config
            int invisibilityDuration = getConfig().getInt("invisibility_duration", 200);
            int invisibilityLevel = getConfig().getInt("invisibility_level", 0);

            killerPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, invisibilityDuration, invisibilityLevel));

            // Send message to killer
            ScenarioLangManager.send(killer, NinjaLang.KILL_INVISIBILITY);
        }
    }
}
