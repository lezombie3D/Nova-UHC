package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.resistance;

import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.ElementType;

import java.util.EnumMap;
import java.util.Map;

public class ResistanceProfile {
    private final Map<ElementType, Double> resistances = new EnumMap<>(ElementType.class);

    public ResistanceProfile() {
        for (ElementType type : ElementType.values()) {
            resistances.put(type, 0.0);
        }
    }

    public void setResistance(ElementType type, double value) {
        resistances.put(type, value);
    }

    public double getResistance(ElementType type) {
        return resistances.getOrDefault(type, 0.0);
    }
}
