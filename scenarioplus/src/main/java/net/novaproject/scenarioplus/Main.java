package net.novaproject.scenarioplus;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.scenarioplus.combat.ArrowScenarios;
import net.novaproject.scenarioplus.combat.DamageScenarios;
import net.novaproject.scenarioplus.combat.KillScenarios;
import net.novaproject.scenarioplus.combat.MeleeScenarios;
import net.novaproject.scenarioplus.craft.CraftScenarios;
import net.novaproject.scenarioplus.craft.EnchantScenarios;
import net.novaproject.scenarioplus.divers.DiversScenarios;
import net.novaproject.scenarioplus.loot.ChestScenarios;
import net.novaproject.scenarioplus.loot.DropScenarios;
import net.novaproject.scenarioplus.loot.InventoryScenarios;
import net.novaproject.scenarioplus.loot.SocialScenarios;
import net.novaproject.scenarioplus.minage.MiningHazardScenarios;
import net.novaproject.scenarioplus.mobs.MobBehaviorScenarios;
import net.novaproject.scenarioplus.monde.BiomeScenarios;
import net.novaproject.scenarioplus.monde.FloodScenarios;
import net.novaproject.scenarioplus.monde.StructureScenarios;
import net.novaproject.scenarioplus.monde.TerrainScenarios;
import net.novaproject.scenarioplus.thematique.ThematiqueScenarios;
import net.novaproject.scenarioplus.mobs.MobDropScenarios;
import net.novaproject.scenarioplus.mobs.MobSpawnScenarios;
import net.novaproject.scenarioplus.minage.OreScenarios;
import net.novaproject.scenarioplus.regles.RegleScenarios;
import net.novaproject.scenarioplus.timers.TimerScenarios;
import net.novaproject.scenarioplus.vie.AbsorptionScenarios;
import net.novaproject.scenarioplus.vie.HealthScenarios;
import net.novaproject.scenarioplus.vie.RegenScenarios;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                registerScenarios();
                LangManager.get().importShipped(Main.this);
                LangManager.get().requestReload();
            }
        }.runTaskLater(this, 20);
    }

    private void registerScenarios() {
        List<Scenario> scenarios = new ArrayList<>();
        scenarios.addAll(RegleScenarios.all());
        scenarios.addAll(OreScenarios.all());
        scenarios.addAll(MiningHazardScenarios.all());
        scenarios.addAll(CraftScenarios.all());
        scenarios.addAll(EnchantScenarios.all());
        scenarios.addAll(TimerScenarios.all());
        scenarios.addAll(MeleeScenarios.all());
        scenarios.addAll(ArrowScenarios.all());
        scenarios.addAll(DamageScenarios.all());
        scenarios.addAll(KillScenarios.all());
        scenarios.addAll(HealthScenarios.all());
        scenarios.addAll(AbsorptionScenarios.all());
        scenarios.addAll(RegenScenarios.all());
        scenarios.addAll(MobSpawnScenarios.all());
        scenarios.addAll(MobDropScenarios.all());
        scenarios.addAll(MobBehaviorScenarios.all());
        scenarios.addAll(DropScenarios.all());
        scenarios.addAll(ChestScenarios.all());
        scenarios.addAll(InventoryScenarios.all());
        scenarios.addAll(SocialScenarios.all());
        scenarios.addAll(TerrainScenarios.all());
        scenarios.addAll(FloodScenarios.all());
        scenarios.addAll(StructureScenarios.all());
        scenarios.addAll(BiomeScenarios.all());
        scenarios.addAll(DiversScenarios.all());
        scenarios.addAll(ThematiqueScenarios.all());

        ScenarioManager manager = ScenarioManager.get();
        for (Scenario scenario : scenarios) {
            manager.addScenario(scenario);
        }
        getLogger().info("ScenarioPlus : " + scenarios.size() + " scenarios enregistres.");
    }
}
