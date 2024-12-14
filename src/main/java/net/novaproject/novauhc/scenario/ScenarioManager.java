package net.novaproject.novauhc.scenario;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.normal.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScenarioManager {

    public static ScenarioManager get(){
        return UHCManager.get().getScenarioManager();
    }

    private final List<Scenario> scenarios = new ArrayList<>();


    public void setup(){
        new Cutclean();
        new HasteyBoy();
        new Rodless();
        new Timber();
        new DouleOre();
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

    public void addScenario(Scenario scenario){
        scenarios.add(scenario);
    }

}
