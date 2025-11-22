package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.status;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.DragonRole;
import org.bukkit.entity.Player;

@Getter
@Setter
public abstract class StatusEffect {

    private final String name;
    private boolean ended;
    private final Player target;
    private final DragonRole dragon;
    private int duration;

    public StatusEffect(Player target, String name, int duration, DragonRole dragon) {
        this.name = name;
        this.target = target;
        this.duration = duration;
        this.dragon = dragon;
        this.ended = false;
    }

    public String getName() {
        return name;
    }

    public boolean isEnded() {
        return ended;
    }

    public void start() {
    }

    public void tick() {
        if (!getTarget().isOnline()) {
            end();
            return;
        }
        minus();
        if (getDuration() <= 0) end();
    }

    public void end() {
        this.ended = true;
    }

    public void minus() {
        duration--;
    }

}
