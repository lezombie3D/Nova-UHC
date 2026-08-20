package net.novaproject.novauhc.utils.nms;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerConnection;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.novaproject.novauhc.display.TabFreezeService;
import net.novaproject.novauhc.Main;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DisguiseService {

    private static final DisguiseService INSTANCE = new DisguiseService();

    public static DisguiseService get() {
        return INSTANCE;
    }


    private static final class Backup {
        final String name;
        final List<Property> textures = new ArrayList<>();
        Backup(String name) { this.name = name; }
    }

    private final Map<UUID, Backup> active = new HashMap<>();

    public boolean isDisguised(UUID uuid) {
        return active.containsKey(uuid);
    }

    public void disguise(Player subject, Player source) {
        GameProfile subjectProf = profile(subject);
        Backup sourceIdentity = active.containsKey(source.getUniqueId())
                ? active.get(source.getUniqueId())
                : backup(profile(source));
        active.computeIfAbsent(subject.getUniqueId(), u -> backup(subjectProf));
        applyBackup(subjectProf, sourceIdentity);
        subject.setDisplayName(sourceIdentity.name);
        subject.setPlayerListName(sourceIdentity.name);
        PacketService.respawnPlayerEntity(subject);
    }

    public void disguise(Player subject, Player source, int durationSec) {
        disguise(subject, source);
        Bukkit.getScheduler().runTaskLater(Main.get(), () -> undisguise(subject), 20L * durationSec);
    }

    public void disguiseSkin(Player subject, String sourceName) {
        Player source = Bukkit.getPlayerExact(sourceName);
        if (source != null) disguise(subject, source);
    }

    public void undisguise(Player subject) {
        Backup backup = active.remove(subject.getUniqueId());
        if (backup == null) return;
        GameProfile prof = profile(subject);
        prof.getProperties().removeAll("textures");
        for (Property p : backup.textures) prof.getProperties().put("textures", copy(p));
        setName(prof, backup.name);
        subject.setDisplayName(backup.name);
        subject.setPlayerListName(backup.name);
        PacketService.respawnPlayerEntity(subject);
    }

    public void clearAll() {
        active.clear();
    }

    private static GameProfile profile(Player player) {
        return ((CraftPlayer) player).getHandle().getProfile();
    }

    private static Backup backup(GameProfile prof) {
        Backup b = new Backup(prof.getName());
        for (Property p : prof.getProperties().get("textures")) b.textures.add(copy(p));
        return b;
    }

    private static void applyTextures(GameProfile target, GameProfile source) {
        target.getProperties().removeAll("textures");
        for (Property p : source.getProperties().get("textures")) target.getProperties().put("textures", copy(p));
    }

    private static void applyBackup(GameProfile target, Backup identity) {
        target.getProperties().removeAll("textures");
        for (Property p : identity.textures) target.getProperties().put("textures", copy(p));
        setName(target, identity.name);
    }

    private static Property copy(Property p) {
        return new Property(p.getName(), p.getValue(), p.getSignature());
    }

    private static void setName(GameProfile profile, String name) {
        try {
            Field f = GameProfile.class.getDeclaredField("name");
            f.setAccessible(true);
            f.set(profile, name);
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[NovaUHC] DisguiseService : impossible de set GameProfile.name : " + t);
        }
    }

    public static final class PacketService {


        public static void respawnPlayerEntity(Player subject) {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                respawnPlayerEntityFor(viewer, subject);
            }
        }

        public static void respawnPlayerEntityFor(Player viewer, Player subject) {
            if (TabFreezeService.isFrozen(subject.getUniqueId()) && !viewer.canSee(subject)) return;
            EntityPlayer nms = ((CraftPlayer) subject).getHandle();
            PlayerConnection conn = ((CraftPlayer) viewer).getHandle().playerConnection;
            conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, nms));
            conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, nms));
            if (!viewer.equals(subject)) {
                conn.sendPacket(new PacketPlayOutEntityDestroy(nms.getId()));
                conn.sendPacket(new PacketPlayOutNamedEntitySpawn(nms));
            }
        }
    }
}
