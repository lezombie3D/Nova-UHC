package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.status;

public abstract class StatusEffect {

    private final String name;
    private boolean ended;

    public StatusEffect(String name) {
        this.name = name;
        this.ended = false;
    }

    public String getName() {
        return name;
    }

    public boolean isEnded() {
        return ended;
    }

    public void start() {
        // Override si besoin
    }

    public void tick() {
        // Doit être implémenté dans les sous-classes
    }

    public void end() {
        this.ended = true;
    }
}
