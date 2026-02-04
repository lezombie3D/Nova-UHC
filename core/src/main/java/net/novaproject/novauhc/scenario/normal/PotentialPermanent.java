package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.lang.PotentialPermanentLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PotentialPermanent extends Scenario {

    private final Map<UUID, Double> permanentHealth = new HashMap<>();
    private final Map<UUID, Double> absorptionHealth = new HashMap<>();

    /* =========================
       SCENARIO VARIABLES
       ========================= */

    @ScenarioVariable(name = "Starting Permanent Health",
            description = "Vie permanente de départ pour chaque joueur (en cœurs).",
            type = VariableType.DOUBLE)
    private double startingPermanentHealth = 20.0;

    @ScenarioVariable(name = "Starting Absorption Health",
            description = "Points d'absorption de départ pour chaque joueur (en cœurs).",
            type = VariableType.DOUBLE)
    private double startingAbsorptionHealth = 20.0;

    @ScenarioVariable(name = "Kill Reward",
            description = "Quantité d'absorption convertie en vie permanente lorsqu'un joueur tue quelqu'un (en cœurs).",
            type = VariableType.DOUBLE)
    private double killReward = 4.0;

    @ScenarioVariable(name = "Max Permanent Health",
            description = "Vie permanente maximale qu'un joueur peut avoir (en cœurs).",
            type = VariableType.DOUBLE)
    private double maxPermanentHealth = 60.0;

    @ScenarioVariable(name = "Max Absorption Health",
            description = "Absorption maximale qu'un joueur peut avoir (en cœurs).",
            type = VariableType.DOUBLE)
    private double maxAbsorptionHealth = 40.0;

    /* =========================
       META
       ========================= */

    @Override
    public String getName() {
        return "PotentialPermanent";
    }

    @Override
    public String getDescription() {
        return "Commencez avec 10 cœurs + 10 d'absorption qui peuvent devenir permanents.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLDEN_APPLE);
    }

    @Override
    public String getPath() {
        return "potentialpermanent";
    }

    @Override
    public ScenarioLang[] getLang() {
        return PotentialPermanentLang.values();
    }


    @Override
    public void onStart(Player player) {
        if (!isActive()) return;

        UUID uuid = player.getUniqueId();

        permanentHealth.put(uuid, startingPermanentHealth);
        absorptionHealth.put(uuid, startingAbsorptionHealth);

        applyHealth(player);

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%permanent_hearts%", startingPermanentHealth / 2);
        placeholders.put("%absorption_hearts%", startingAbsorptionHealth / 2);

        ScenarioLangManager.send(uhcPlayer, PotentialPermanentLang.STARTING_HEALTH, placeholders);
        ScenarioLangManager.send(uhcPlayer, PotentialPermanentLang.CONVERSION_INFO);
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

        k.sendMessage("§e[PotentialPermanent] §f+" + (reward / 2) + " cœur(s) permanent(s)");
        Bukkit.broadcastMessage("§e[PotentialPermanent] §f" + k.getName() +
                " a maintenant §c" + (perm / 2) + " cœurs permanents");
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
