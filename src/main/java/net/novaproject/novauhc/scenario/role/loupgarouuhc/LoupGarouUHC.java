package net.novaproject.novauhc.scenario.role.loupgarouuhc;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.loupgarouuhc.roles.LoupGarou;
import net.novaproject.novauhc.utils.ItemCreator;

public class LoupGarouUHC extends ScenarioRole<LoupGarouRole> {

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return null;
    }

    @Override
    public void setup() {

        addRole(new LoupGarou());

    }
}
