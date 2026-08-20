package net.novaproject.novauhc.scenario.random;

import net.novaproject.novauhc.utils.variable.Variables;import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.lang.lang.RandomEventLang;
import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.variable.Variables.VariableSerializer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bson.Document;

@Getter
@Setter
public abstract class RandomGameEvent<S extends Scenario> {

    @Var(lang = RandomEventLang.class, nameKey = "ENABLED_NAME", descKey = "ENABLED_DESC", type = VariableType.BOOLEAN)
    private boolean enabled = false;

    @Var(lang = RandomEventLang.class, nameKey = "CHANCE_NAME", descKey = "CHANCE_DESC", type = VariableType.PERCENTAGE, min = 0, max = 1)
    private double chance = 1.0;

    @Var(lang = RandomEventLang.class, nameKey = "MIN_TIME_NAME", descKey = "MIN_TIME_DESC", type = VariableType.TIME)
    private int minGameTime = 0;

    @Var(lang = RandomEventLang.class, nameKey = "MAX_TIME_NAME", descKey = "MAX_TIME_DESC", type = VariableType.TIME)
    private int maxGameTime = 3600;

    @Var(lang = RandomEventLang.class, nameKey = "REPEATING_NAME", descKey = "REPEATING_DESC", type = VariableType.BOOLEAN)
    private boolean repeating = false;

    private S scenario;

    public abstract void execute();

    public String getName() {
        return getClass().getSimpleName();
    }

    public boolean canFire() {
        return true;
    }

    public boolean isTriggered() {
        return false;
    }

    public Document toDoc() {
        return VariableSerializer.toDoc(this, Variables.of(this));
    }

    public void fromDoc(Document doc) {
        VariableSerializer.fromDoc(this, doc, Variables.of(this));
    }
}