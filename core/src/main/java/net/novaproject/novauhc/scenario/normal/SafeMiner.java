package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class SafeMiner extends Scenario {


    @ScenarioVariable(
            name = "Max Height",
            description = "Hauteur maximale (Y) en dessous de laquelle les joueurs sont protégés.",
            type = VariableType.INTEGER
    )
    private int max_height = 32;

    @ScenarioVariable(
            name = "Disable At PvP",
            description = "Désactive SafeMiner automatiquement à l'activation du PvP.",
            type = VariableType.BOOLEAN
    )
    private boolean disable_at_pvp = true;

    private boolean actived = true;

    @Override
    public String getName() {
        return "SafeMiner";
    }

    @Override
    public String getDescription() {
        return "Protège les joueurs des dégâts de lave et d'étouffement en minant.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND_PICKAXE);
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (!(entity instanceof Player player)) return;

        if (player.getLocation().getY() <= max_height) {
            event.setCancelled(true);
        }
    }

    @Override
    public String getPath() {
        return "safeminer";
    }

    @Override
    public void onSec(Player p) {
        if (!disable_at_pvp) return;

        int timer = UHCManager.get().getTimer();
        int timerpvp = UHCManager.get().getTimerpvp();

        if (timer == timerpvp) {
            actived = false;
        }
    }
}
