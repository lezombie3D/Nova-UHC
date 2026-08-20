package net.novaproject.novauhc.player.utils;

import net.novaproject.novauhc.debug.DebugLog;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerKillEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerReconnectEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerTimeoutEvent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.novaproject.novauhc.player.PlayerSpi.Combat;
import net.novaproject.novauhc.player.PlayerSpi.IUHCPlayer;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.cooldown.CooldownService.CombatTracker;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ZombieCombat implements Combat, Listener {

    public static final ZombieCombat INSTANCE = new ZombieCombat();

    public final String NPC_METADATA = "combatlog_owner";

    private final Map<UUID, LivingEntity> npcsByPlayer = new HashMap<>();
    private final Map<UUID, UUID> npcOwners = new HashMap<>();

    @Override
    public void onTag(IUHCPlayer attacker, IUHCPlayer victim) {}

    @Override
    public void onQuit(IUHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();

        if (npcsByPlayer.containsKey(player.getUniqueId())) return;

        LivingEntity npc = spawnNpc(player);
        if (npc == null) return;

        npcsByPlayer.put(player.getUniqueId(), npc);
        npcOwners.put(npc.getUniqueId(), player.getUniqueId());
    }

    public boolean hasNpc(UUID playerUuid) {
        return npcsByPlayer.containsKey(playerUuid);
    }

    @EventHandler
    public void onNpcDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        UUID owner = npcOwners.remove(entity.getUniqueId());
        if (owner == null) return;
        npcsByPlayer.remove(owner);

        event.getDrops().clear();
        event.setDroppedExp(0);

        Player killer = entity.getKiller();
        String victimName = Bukkit.getOfflinePlayer(owner).getName();
        String killerName = killer != null ? killer.getName() : resolveLastDamagerName(owner);

        if (killer != null) {
            UHCManager.get().getStatsTracker().addKill(killer.getUniqueId());
            UHCPlayer killerUHC = UHCPlayerManager.get().getPlayer(killer);
            UHCPlayer victimUHC = UHCPlayerManager.get().getPlayer(owner);
            if (killerUHC != null && victimUHC != null) {
                killerUHC.setKill(killerUHC.getKill() + 1);
                Bukkit.getPluginManager().callEvent(
                        new UhcPlayerKillEvent(killerUHC, victimUHC));
            }
        }

        Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_COMBAT_LOG_KILLED, null,
                Map.of("%player%", victimName == null ? "?" : victimName, "%killer%", killerName)));

        ReconnectionManager.get().eliminateNow(owner, entity.getLocation());
    }


    @EventHandler
    public void onReconnect(UhcPlayerReconnectEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        LivingEntity npc = npcsByPlayer.remove(uuid);
        if (npc == null) return;
        npcOwners.remove(npc.getUniqueId());

        Player player = event.getPlayer().getPlayer();
        if (player != null && npc.isValid()) {
            double health = Math.max(1.0, Math.min(npc.getHealth(), player.getMaxHealth()));
            player.setHealth(health);
        }
        removeNpc(npc);
        Bukkit.getLogger().info("[CombatLog] NPC retiré (reconnexion de " + (player != null ? player.getName() : uuid) + ")");
    }

    @EventHandler
    public void onTimeout(UhcPlayerTimeoutEvent event) {
        LivingEntity npc = npcsByPlayer.remove(event.getUuid());
        if (npc == null) return;
        npcOwners.remove(npc.getUniqueId());
        removeNpc(npc);
    }

    private String resolveLastDamagerName(UUID owner) {
        UUID last = CombatTracker.getLastDamager(owner);
        if (last == null) return "?";
        String name = Bukkit.getOfflinePlayer(last).getName();
        return name == null ? "?" : name;
    }

    private LivingEntity spawnNpc(Player player) {
        try {
            Location location = player.getLocation();
            if (location.getWorld() == null) return null;

            LivingEntity citizens = spawnCitizensBody(player);
            if (citizens != null) {
                citizens.setMetadata(NPC_METADATA,
                        new FixedMetadataValue(Main.get(), player.getUniqueId().toString()));
                return citizens;
            }

            Zombie zombie = location.getWorld().spawn(location, Zombie.class);
            zombie.setCustomName("§c" + player.getName());
            zombie.setCustomNameVisible(true);
            zombie.setRemoveWhenFarAway(false);
            zombie.setCanPickupItems(false);
            zombie.setMaxHealth(Math.max(1.0, player.getMaxHealth()));
            zombie.setHealth(Math.max(0.5, Math.min(player.getHealth(), zombie.getMaxHealth())));

            PlayerInventory inventory = player.getInventory();
            EntityEquipment equipment = zombie.getEquipment();
            equipment.setHelmet(cloneOr(inventory.getHelmet(), new ItemStack(Material.LEATHER_HELMET)));
            equipment.setChestplate(cloneOr(inventory.getChestplate(), null));
            equipment.setLeggings(cloneOr(inventory.getLeggings(), null));
            equipment.setBoots(cloneOr(inventory.getBoots(), null));
            equipment.setItemInHand(cloneOr(inventory.getItemInHand(), null));
            equipment.setHelmetDropChance(0f);
            equipment.setChestplateDropChance(0f);
            equipment.setLeggingsDropChance(0f);
            equipment.setBootsDropChance(0f);
            equipment.setItemInHandDropChance(0f);

            applyNoAi(zombie);
            zombie.setMetadata(NPC_METADATA, new FixedMetadataValue(Main.get(), player.getUniqueId().toString()));
            return zombie;
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.SEVERE, "[CombatLog] spawn du NPC impossible pour " + player.getName(), t);
            return null;
        }
    }

    private LivingEntity spawnCitizensBody(Player player) {
        if (!CitizensNpcs.present()) return null;
        try {
            LivingEntity body = CitizensNpcs.spawnBody(player);
            if (body == null) return null;
            body.setMaxHealth(Math.max(1.0, player.getMaxHealth()));
            body.setHealth(Math.max(0.5, Math.min(player.getHealth(), body.getMaxHealth())));
            body.setRemoveWhenFarAway(false);
            return body;
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING,
                    "[CombatLog] NPC Citizens impossible, repli sur le zombie : " + t.getMessage(), t);
            return null;
        }
    }

    private void removeNpc(LivingEntity npc) {
        if (CitizensNpcs.present()) {
            try {
                CitizensNpcs.remove(npc);
            } catch (Throwable error) {
                DebugLog.warnOnce("CombatLog", "echec silencieux", error);
            }
        }
        npc.remove();
    }

    private ItemStack cloneOr(ItemStack item, ItemStack fallback) {
        if (item == null || item.getType() == Material.AIR) return fallback;
        return item.clone();
    }

    private void applyNoAi(Zombie zombie) {
        try {
            Entity nms = ((CraftEntity) zombie).getHandle();
            NBTTagCompound tag = nms.getNBTTag();
            if (tag == null) tag = new NBTTagCompound();
            nms.c(tag);
            tag.setByte("NoAI", (byte) 1);
            tag.setByte("Silent", (byte) 1);
            nms.f(tag);
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING, "[CombatLog] NoAI inapplicable sur le NPC : " + t.getMessage(), t);
        }
    }

}
