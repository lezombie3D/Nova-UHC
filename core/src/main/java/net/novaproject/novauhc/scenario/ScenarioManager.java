package net.novaproject.novauhc.scenario;

import net.novaproject.novauhc.utils.variable.Variables;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.normal.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScenarioManager implements ScenarioSpi.IScenarioManager {

    public static ScenarioManager get(){
        return UHCManager.get().getScenarioManager();
    }

    private final List<Scenario> scenarios = new ArrayList<>();

    public void setup(){
        addScenario(new AcidRain());
        addScenario(new CombatScenarios.BatRoulette());
        addScenario(new BestPvE());
        addActiveScenario(new CombatScenarios.BetaZombie());
        addScenario(new MiningScenarios.BlackArrow());
        addScenario(new Blizzard());
        addScenario(new BlockRush());
        addScenario(new BloodCycle());
        addScenario(new MiningScenarios.BloodDiamonds());
        addScenario(new BloodLust());
        addScenario(new KillRewards.BuffKiller());
        addActiveScenario(new PlayerBoosts.CatEye());
        addActiveScenario(new Restrictions.ChatPvP());
        addActiveScenario(new MiningScenarios.CobbleUnified());
        addScenario(new KillRewards.Compensation());
        addScenario(new CombatScenarios.Cripple());
        addActiveScenario(new Cutclean());
        addScenario(new MiningScenarios.DeathEmerauld());
        addScenario(new Democracy());
        addScenario(new DoubleOre());
        addActiveScenario(new Restrictions.EndLess());
        addScenario(new Fallout());
        addScenario(new ScheduledScenarios.FastFurnace());
        addScenario(new PlayerBoosts.FastMiner());
        addActiveScenario(new ScheduledScenarios.FinalHeal());
        addScenario(new Restrictions.FireLess());
        addScenario(new GapRoulette());
        addScenario(new Genie());
        addScenario(new Gladiator());
        addScenario(new KillRewards.GoldenDrop());
        addScenario(new GoldenHead());
        addScenario(new HasteyTools.HasteyAlpha());
        addScenario(new HasteyTools.HasteyBabie());
        addActiveScenario(new HasteyTools.HasteyBoy());
        addScenario(new ScheduledScenarios.HeathCharity());
        addScenario(new Restrictions.HorseLess());
        addScenario(new Restrictions.InfiniteEnchanter());
        addScenario(new Inventors());
        addScenario(new Restrictions.Lavaless());
        addScenario(new CombatScenarios.LongShoot());
        addScenario(new LootCrate());
        addScenario(new LuckyOre());
        addActiveScenario(new Magnet());
        addScenario(new Meetup());
        addActiveScenario(new Restrictions.NetherLess());
        addScenario(new KillRewards.Ninja());
        addScenario(new NineSlot());
        addScenario(new KillRewards.NoCleanUp());
        addScenario(new Restrictions.NoFall());
        addScenario(new Restrictions.NoFood());
        addScenario(new Restrictions.NoNameTag());
        addScenario(new Restrictions.NoNotchGap());
        addScenario(new Restrictions.NoWoodenTool());
        addScenario(new ObsiBuff());
        addScenario(new OreRoulette());
        addScenario(new OreSwap());
        addScenario(new ParkourMaster());
        addScenario(new PotentialPermanent());
        addScenario(new Quiver());
        addScenario(new Restrictions.Rodless());
        addActiveScenario(new CombatScenarios.SafeMiner());
        addScenario(new SimonSays());
        addScenario(new PlayerBoosts.StarterTools());
        addScenario(new CombatScenarios.SwitchArrow());
        addScenario(new TeamArrow());
        addScenario(new TeamInv());
        addScenario(new MiningScenarios.Timber());
        addScenario(new TimeBombe());
        addScenario(new ScheduledScenarios.TpMeetup());
        addScenario(new Transmutation());
        addScenario(new Vampire());
        addScenario(new WeakestLink());
        addScenario(new KillRewards.WebCage());
        addScenario(new PlayerBoosts.WoolToString());
        addScenario(new ScheduledScenarios.XpSansue());
    }

    private void addActiveScenario(Scenario scenario){
        addScenario(scenario);
        scenario.enable();
    }

    public Optional<Scenario> getScenarioByName(String name){
        for(Scenario s : scenarios){
            if(s.getName().equalsIgnoreCase(name)){
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public List<Scenario> getScenarios(){
        return scenarios;
    }

    public List<Scenario> getActiveScenarios(){
        List<Scenario> result = new ArrayList<>();
        for(Scenario s : scenarios){
            if(s.isActive()){
                result.add(s);
            }
        }
        return result;
    }

    public boolean isScenarioActive(String scenarioName) {
        return getScenarioByName(scenarioName).map(Scenario::isActive).orElse(false);
    }

    public List<Scenario> getSpecialScenarios() {
        List<Scenario> result = new ArrayList<>();
        for (Scenario s : scenarios) {
            if (s.isSpecial()) {
                result.add(s);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Scenario> T getScenario(Class<T> clazz) {
        for (Scenario scenario : scenarios) {
            if (clazz.isInstance(scenario)) {
                return (T) scenario;
            }
        }
        return null;
    }

    public List<Scenario> getActiveSpecialScenarios() {
        List<Scenario> result = new ArrayList<>();
        for (Scenario s : getSpecialScenarios()) {
            if (s.isActive()) {
                result.add(s);
            }
        }
        return result;
    }

    public void addScenario(Scenario scenario){
        scenarios.add(scenario);
        scenario.setup();

        Variables.deepScan(scenario);
        if (scenario instanceof ScenarioRole<?> scenarioRole) {
            scenarioRole.getRoleConfigs().values()
                    .forEach(Variables::deepScan);
        }
        if (booted && LangManager.get() != null) {
            LangManager.get().requestReload();
        }
    }

    private boolean booted = false;

    public void markBooted() {
        booted = true;
    }
}