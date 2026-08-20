package net.novaproject.novauhc.utils.nms;

import net.novaproject.novauhc.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class MobDisguiseService {

    private static final MobDisguiseService INSTANCE = new MobDisguiseService();

    private MobDisguiseService() {
    }

    public static MobDisguiseService get() {
        return INSTANCE;
    }

    public static void start() {
        MobDisguisePackets.start();
    }

    public boolean supports(EntityType type) {
        return MobDisguisePackets.supports(type);
    }

    public boolean isDisguised(UUID uuid) {
        return MobDisguisePackets.isDisguised(uuid);
    }

    public void disguise(Player subject, EntityType type) {
        MobDisguisePackets.disguise(subject, type);
    }

    public void disguise(Player subject, EntityType type, int durationSec) {
        MobDisguisePackets.disguise(subject, type);
        Bukkit.getScheduler().runTaskLater(Main.get(), () -> undisguise(subject), 20L * durationSec);
    }

    public void undisguise(Player subject) {
        MobDisguisePackets.undisguise(subject);
    }

    public void disguiseAllMobs(EntityType type) {
        MobDisguisePackets.disguiseAllMobs(type);
    }

    public void undisguiseAllMobs() {
        MobDisguisePackets.undisguiseAllMobs();
    }

    public EntityType disguisedMobType() {
        return MobDisguisePackets.disguisedMobType();
    }

    public void clearAll() {
        MobDisguisePackets.clearAll();
    }
}
