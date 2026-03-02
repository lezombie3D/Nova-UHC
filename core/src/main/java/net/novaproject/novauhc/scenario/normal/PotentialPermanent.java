package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.lang.LangManager;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.lang.scenario.PotentialPermanentLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;

public class PotentialPermanent extends Scenario {

    private final Map<UUID, Double> permanentHealth = new HashMap<>();
    private final Map<UUID, Double> absorptionHealth = new HashMap<>();

    

    @ScenarioVariable(lang = ScenarioVarLang.class, nameKey = "POTENTIALPERMANENT_VAR_STARTING_PERMANENT_HEALTH_NAME", descKey = "POTENTIALPERMANENT_VAR_STARTING_PERMANENT_HEALTH_DESC", type = VariableType.DOUBLE)
    private final double startingPermanentHealth = 20.0;

    @ScenarioVariable(lang = ScenarioVarLang.class, nameKey = "POTENTIALPERMANENT_VAR_STARTING_ABSORPTION_HEALTH_NAME", descKey = "POTENTIALPERMANENT_VAR_STARTING_ABSORPTION_HEALTH_DESC", type = VariableType.DOUBLE)
    private final double startingAbsorptionHealth = 20.0;

    @ScenarioVariable(lang = ScenarioVarLang.class, nameKey = "POTENTIALPERMANENT_VAR_KILL_REWARD_NAME", descKey = "POTENTIALPERMANENT_VAR_KILL_REWARD_DESC", type = VariableType.DOUBLE)
    private final double killReward = 4.0;

    @ScenarioVariable(lang = ScenarioVarLang.class, nameKey = "POTENTIALPERMANENT_VAR_MAX_PERMANENT_HEALTH_NAME", descKey = "POTENTIALPERMANENT_VAR_MAX_PERMANENT_HEALTH_DESC", type = VariableType.DOUBLE)
    private final double maxPermanentHealth = 60.0;

    @ScenarioVariable(lang = ScenarioVarLang.class, nameKey = "POTENTIALPERMANENT_VAR_MAX_ABSORPTION_HEALTH_NAME", descKey = "POTENTIALPERMANENT_VAR_MAX_ABSORPTION_HEALTH_DESC", type = VariableType.DOUBLE)
    private final double maxAbsorptionHealth = 40.0;

    

    @Override
    public String getName() {
        return "PotentialPermanent";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.POTENTIAL_PERMANENT, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLDEN_APPLE);
    }




    @Override
    public void onStart(Player player) {
        if (!isActive()) return;

        UUID uuid = player.getUniqueId();

        permanentHealth.put(uuid, startingPermanentHealth);
        absorptionHealth.put(uuid, startingAbsorptionHealth);

        applyHealth(player);


        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%permanent_hearts%", startingPermanentHealth / 2);
        placeholders.put("%absorption_hearts%", startingAbsorptionHealth / 2);

        LangManager.get().send(PotentialPermanentLang.STARTING_HEALTH, player, placeholders);
        LangManager.get().send(PotentialPermanentLang.CONVERSION_INFO, player);
    }

    @Override
    public void onDeath(UHCPlayer dead, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        permanentHealth.remove(dead.getPlayer().getUniqueId());
        absorptionHealth.remove(dead.getPlayer().getUniqueId());

        if (killer == null) return;

        Player k = killer.getPlayer();
        UUID uuid = k.getUniqueId();

        double perm = permanentHealth.getOrDefault(uuid, startingPermanentHealth);
        double absorp = absorptionHealth.getOrDefault(uuid, 0.0);

        double reward = Math.min(killReward, absorp);

        perm = Math.min(maxPermanentHealth, perm + reward);
        absorp -= reward;

        permanentHealth.put(uuid, perm);
        absorptionHealth.put(uuid, absorp);

        applyHealth(k);

        LangManager.get().send(PotentialPermanentLang.KILL_REWARD, k, Map.of("%hearts%", String.valueOf(reward / 2)));
        LangManager.get().sendAll(PotentialPermanentLang.KILL_BROADCAST, Map.of("%player%", k.getName(), "%hearts%", String.valueOf(perm / 2)));
    }

    private void applyHealth(Player player) {
        UUID uuid = player.getUniqueId();

        double perm = permanentHealth.getOrDefault(uuid, startingPermanentHealth);
        double absorp = absorptionHealth.getOrDefault(uuid, 0.0);

        player.setMaxHealth(perm);
        player.setHealth(Math.min(player.getHealth(), perm));

        player.removePotionEffect(PotionEffectType.ABSORPTION);

        if (absorp > 0) {
            int level = (int) Math.ceil(absorp / 4.0) - 1;
            level = Math.max(0, Math.min(level, 19));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, level));
        }
    }
}
