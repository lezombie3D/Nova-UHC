package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.status;

import net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall.status.StatusEffect;
import org.bukkit.Effect;
import org.bukkit.entity.Player;

public class FireBlight extends StatusEffect {

    private final Player target;
    private int ticks;

    public FireBlight(Player target) {
        super("FireBlight");
        this.target = target;
        this.ticks = 5; // 5 secondes
    }

    @Override
    public void start() {
        target.sendMessage("§c🔥 Vous êtes en proie aux flammes !");
    }

    @Override
    public void tick() {
        if (!target.isOnline()) {
            end();
            return;
        }

        target.getWorld().spigot().playEffect(target.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 5, 30);
        if (ticks % 20 == 0) target.damage(1.0D);

        ticks--;
        if (ticks <= 0) end();
    }

    @Override
    public void end() {
        super.end();
        target.sendMessage("§7🔥 Le feu s'est éteint.");
    }
}
