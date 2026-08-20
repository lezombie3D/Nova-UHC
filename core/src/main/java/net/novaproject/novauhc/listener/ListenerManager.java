package net.novaproject.novauhc.listener;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.debug.UhcEventLogger;
import net.novaproject.novauhc.game.GroupSizeManager;
import net.novaproject.novauhc.game.LateJoinManager;
import net.novaproject.novauhc.game.SpectatorNotifier;
import net.novaproject.novauhc.game.Systems.StatsRoleListener;
import net.novaproject.novauhc.game.Systems.SystemsListener;
import net.novaproject.novauhc.lobby.AutoStartManager;
import net.novaproject.novauhc.lobby.FreezeManager;
import net.novaproject.novauhc.lobby.VanishManager;
import net.novaproject.novauhc.player.utils.CombatLogManager;
import net.novaproject.novauhc.player.utils.ZombieCombat;
import net.novaproject.novauhc.scenario.role.reveal.FacadePerceptionService.RoleRevealListener;
import net.novaproject.novauhc.ui.CustomInventory.CustomInventoryEvent;
import net.novaproject.novauhc.utils.nms.EquipmentHider;
import net.novaproject.novauhc.world.generation.ChunkUnloadListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class ListenerManager {

    public static void setup() {
        PluginManager pm = Bukkit.getPluginManager();
        Main plugin = Main.get();

        pm.registerEvents(new PlayerConnectionEvent(), plugin);
        pm.registerEvents(new PlayerBlockEvent(),      plugin);
        pm.registerEvents(new PlayerCraftEvent(),      plugin);
        pm.registerEvents(new PlayerInteractListener(), plugin);
        pm.registerEvents(new VanillaTweaksListener(), plugin);
        pm.registerEvents(new CombatNerfListener(),    plugin);

        pm.registerEvents(new PlayerMiscListeners.PlayerDeathListener(),     plugin);
        pm.registerEvents(new PlayerMiscListeners.PlayerTakeDamage(),        plugin);
        pm.registerEvents(new PlayerMiscListeners.FoodListener(),            plugin);
        pm.registerEvents(new PlayerMiscListeners.PickupListener(),          plugin);
        pm.registerEvents(new PlayerMiscListeners.MovementListener(),        plugin);
        pm.registerEvents(new PlayerMiscListeners.ChatListener(),            plugin);
        pm.registerEvents(new PlayerMiscListeners.AntiSuffocationListener(), plugin);
        pm.registerEvents(new PlayerMiscListeners.GoldenAppleListener(),     plugin);
        pm.registerEvents(new PlayerMiscListeners.PortalListener(),          plugin);

        pm.registerEvents(new EntityListeners.EntityDeathListener(),      plugin);
        pm.registerEvents(new EntityListeners.EntityBowEvent(),           plugin);
        pm.registerEvents(new EntityListeners.EntityDamageEntity(),       plugin);
        pm.registerEvents(new EntityListeners.EntityProjectileHitEvent(), plugin);

        pm.registerEvents(new WorldListeners.RedstoneBlocker(),      plugin);
        pm.registerEvents(new WorldListeners.SurfaceSpawnListener(), plugin);
        pm.registerEvents(new WorldListeners.WorldInitListener(),    plugin);

        pm.registerEvents(new CombatLogManager(),     plugin);
        pm.registerEvents(new GroupSizeManager(),     plugin);
        pm.registerEvents(new AutoStartManager(),     plugin);
        pm.registerEvents(new RoleRevealListener(),   plugin);
        pm.registerEvents(new StatsRoleListener(),    plugin);
        pm.registerEvents(new FreezeManager(),        plugin);
        pm.registerEvents(new VanishManager(),        plugin);
        pm.registerEvents(new EquipmentHider(),       plugin);
        pm.registerEvents(new LateJoinManager(),      plugin);
        pm.registerEvents(new UhcEventLogger(),       plugin);
        pm.registerEvents(new SystemsListener(),      plugin);
        pm.registerEvents(new SpectatorNotifier(),    plugin);
        pm.registerEvents(new CustomInventoryEvent(), plugin);
        pm.registerEvents(new ChunkUnloadListener(),  plugin);

        pm.registerEvents(ZombieCombat.INSTANCE, plugin);
    }
}
