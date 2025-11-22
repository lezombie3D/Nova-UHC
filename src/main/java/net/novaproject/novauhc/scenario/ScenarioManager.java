package net.novaproject.novauhc.scenario;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.normal.*;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.DragonFall;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScenarioManager {

    public static ScenarioManager get(){
        return UHCManager.get().getScenarioManager();
    }

    private final List<Scenario> scenarios = new ArrayList<>();

    public void setup(){

        addScenario(new Cutclean());
        addScenario(new HasteyBoy());
        addScenario(new Rodless());
        addScenario(new Timber());
        addScenario(new DoubleOre());
        addScenario(new FIreLess());
        addScenario(new BloodDiamonds());
        addScenario(new NoFall());

        addScenario(new WooltoString());
        addScenario(new BetaZombie());
        addScenario(new CatEye());
        addScenario(new Meetup());
        //addScenario(new LoupGarouUHC());
        addScenario(new StarterTools());
        addScenario(new InfiniteEnchanter());

        addScenario(new FinalHeal());
        addScenario(new BlackArrow());
        addScenario(new NoFood());

        addScenario(new BatRoulette());
        addScenario(new WebCage());
        addScenario(new TimeBombe());

        addScenario(new ArrowSwitch());
        addScenario(new NoNether());
        addScenario(new NoEnd());
        addScenario(new LongShoot());
        addScenario(new BloodCycle());
        addScenario(new GapRoulette());

        addScenario(new NoWoodenTool());
        addScenario(new AutoRevive());
        addScenario(new FastFurnace());
        addScenario(new NoCleanUp());
        addScenario(new ChatPvP());
        addScenario(new SafeMiner());
        addScenario(new TeamInv());
        addScenario(new noHorse());
        //addScenario(new CromagnonUHC());
        addScenario(new NoNameTag());
        addScenario(new BestPvE());
        addScenario(new HeathCharity());
        addScenario(new Compensation());
        addScenario(new Cripple());
        addScenario(new DeathEmerauld());
        addScenario(new XpSansue());
        addScenario(new FastMiner());
        addScenario(new GoldenHead());
        addScenario(new GoldenDrop());
        addScenario(new HasteyBabie());
        addScenario(new HasteyAlpha());
        addScenario(new BuffKiller());
        addScenario(new TeamArrow());
        //addScenario(new FireForceUHC());

        addScenario(new Vampire());
        addScenario(new Gladiator());
        addScenario(new BloodLust());
        addScenario(new WeakestLink());


        addScenario(new OreSwap());
        addScenario(new OreRoulette());
        addScenario(new Magnet());
        addScenario(new LuckyOre());
        addScenario(new Transmutation());

        addScenario(new AcidRain());
        addScenario(new Blizzard());

        addScenario(new ParkourMaster());
        addScenario(new SimonSays());
        addScenario(new Democracy());

        addScenario(new Inventors());
        addScenario(new BlockRush());

        addScenario(new NineSlot());
        addScenario(new Fallout());



        addScenario(new Genie());
        addScenario(new PotentialPermanent());
        addScenario(new LootCrate());

        /*addScenario(new BeatTheSanta());
        addScenario(new FallenKingdom());
        addScenario(new FlowerPower());
        addScenario(new GoneFIsh());
        addScenario(new King());
        addScenario(new Legend());
        addScenario(new NetheriBus());
        addScenario(new RandomCraft());
        addScenario(new RandomDrop());
        addScenario(new SkyHigh());
        addScenario(new SlaveMarket());
        addScenario(new SuperHeros());
        addScenario(new TaupeGun());
        addScenario(new TeamAtFirstSeigth());
        addScenario(new SoulBrother());
        addScenario(new MysteryTeam());*/

        //addScenario(new DeathNote());
        addScenario(new DragonFall());
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

    }
}
