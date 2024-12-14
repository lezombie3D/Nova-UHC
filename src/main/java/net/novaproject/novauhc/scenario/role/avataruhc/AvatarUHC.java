package net.novaproject.novauhc.scenario.role.avataruhc;

import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.utils.ItemCreator;

public abstract class AvatarUHC extends ScenarioRole<AvatarRole> {
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
}
