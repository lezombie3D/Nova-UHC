package net.novaproject.novauhc.scenario.role.loupgarouuhc.roles;

import net.novaproject.novauhc.scenario.role.loupgarouuhc.LoupGarouRole;

public class Villageois extends LoupGarouRole {
    @Override
    public String getName() {
        return "Villageois";
    }

    @Override
    public String getDescription() {
        return "Vous etes un simple souffre douleur";
    }

    @Override
    public String getCamps() {
        return "village";
    }
}
