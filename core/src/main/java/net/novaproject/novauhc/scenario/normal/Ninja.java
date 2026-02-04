package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.lang.NinjaLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Ninja extends Scenario {

    @ScenarioVariable(
            name = "Invisibility Duration",
            description = "Durée de l'invisibilité après un kill en ticks (20 ticks = 1 seconde).",
            type = VariableType.TIME
    )
    private int invisibilityDuration = 200;

    @ScenarioVariable(
            name = "Invisibility Level",
            description = "Niveau de l'effet d'invisibilité après un kill.",
            type = VariableType.INTEGER
    )
    private int invisibilityLevel = 0;

    @Override
    public String getName() {
        return "Ninja";
    }

    @Override
    public String getDescription() {
        return "Devenez invisible pendant " + (invisibilityDuration / 20) + " secondes après chaque kill !";
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
            killerPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, invisibilityDuration, invisibilityLevel));
            ScenarioLangManager.send(killer, NinjaLang.KILL_INVISIBILITY);
        }
    }
}
