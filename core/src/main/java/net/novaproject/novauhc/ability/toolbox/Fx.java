package net.novaproject.novauhc.ability.toolbox;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.chat.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.IntConsumer;

public final class Fx {

    public static final class Sounds {

        private Sounds() {
        }

        public static void play(Player player, Sound sound, float volume, float pitch) {
            if (player == null || sound == null) return;
            player.playSound(player.getLocation(), sound, volume, pitch);
        }

        public static void playAt(Location loc, Sound sound, float volume, float pitch) {
            if (loc == null || loc.getWorld() == null || sound == null) return;
            loc.getWorld().playSound(loc, sound, volume, pitch);
        }

        public static void success(Player player) {
            play(player, Sound.ORB_PICKUP, 0.8f, 1.6f);
        }

        public static void fail(Player player) {
            play(player, Sound.NOTE_BASS, 1f, 0.6f);
        }

        public static void pulse(Player player) {
            play(player, Sound.NOTE_PLING, 0.5f, 1.2f);
        }

        public static void levelUp(Player player) {
            play(player, Sound.LEVEL_UP, 0.8f, 1f);
        }
    }

    public static final class Buff {

        private final List<PotionEffectType> types = new ArrayList<>();
        private final List<Integer> amplifiers = new ArrayList<>();
        private int durationSeconds = 0;
        private boolean looped = false;

        public static Buff builder() {
            return new Buff();
        }

        public Buff add(PotionEffectType type, int amplifier) {
            if (type != null) {
                types.add(type);
                amplifiers.add(amplifier);
            }
            return this;
        }

        public Buff duration(int seconds) {
            this.durationSeconds = Math.max(0, seconds);
            return this;
        }

        public Buff looped() {
            this.looped = true;
            return this;
        }

        private PotionEffect[] build() {
            int ticks = Math.max(1, durationSeconds * 20);
            PotionEffect[] arr = new PotionEffect[types.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = new PotionEffect(types.get(i), ticks, amplifiers.get(i));
            }
            return arr;
        }

        public void apply(Player player) {
            if (player == null) return;
            if (looped) {
                applyTick(player);
                return;
            }
            for (PotionEffect effect : build()) {
                player.addPotionEffect(effect, true);
            }
        }

        public void applyTick(Player player) {
            if (player == null) return;
            UHCUtils.applyInfiniteEffects(build(), player);
        }
    }

    public static final class Countdown {

        private final UUID uuid;
        private final int seconds;
        private IntConsumer onTick;
        private Runnable onEnd;
        private int secondsLeft;
        private BukkitTask task;

        public Countdown(Player player, int seconds) {
            this.uuid = player.getUniqueId();
            this.seconds = Math.max(0, seconds);
            this.secondsLeft = this.seconds;
        }

        public Countdown onTick(IntConsumer onTick) {
            this.onTick = onTick;
            return this;
        }

        public Countdown onEnd(Runnable onEnd) {
            this.onEnd = onEnd;
            return this;
        }

        public Countdown start() {
            if (task != null) return this;
            if (seconds <= 0) {
                if (onEnd != null) onEnd.run();
                return this;
            }
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) {
                        cancel();
                        task = null;
                        return;
                    }
                    DisplayService.actionBar(player, "§e§l" + TextUtils.getFormattedTime(secondsLeft));
                    if (onTick != null) onTick.accept(secondsLeft);
                    secondsLeft--;
                    if (secondsLeft < 0) {
                        cancel();
                        task = null;
                        if (onEnd != null) onEnd.run();
                    }
                }
            }.runTaskTimer(Main.get(), 0L, 20L);
            return this;
        }

        public void stop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }

    public static final class CustomProjectile {

        public static <T extends Projectile> T launch(Player owner, Class<T> type, String metaKey, Vector velocity) {
            T proj = owner.launchProjectile(type);
            proj.setShooter(owner);
            proj.setMetadata(metaKey, new FixedMetadataValue(Main.get(), owner.getUniqueId().toString()));
            if (velocity != null) {
                proj.setVelocity(velocity);
            }
            if (proj instanceof Explosive ex) {
                ex.setYield(0f);
                ex.setIsIncendiary(false);
            }
            return proj;
        }

        public static UUID ownerUuid(Entity entity, String metaKey) {
            if (entity == null || !entity.hasMetadata(metaKey)) return null;
            for (MetadataValue v : entity.getMetadata(metaKey)) {
                try {
                    return UUID.fromString(v.asString());
                } catch (IllegalArgumentException ignored) {

                }
            }
            return null;
        }

        public static boolean isFrom(Entity entity, String metaKey, Player owner) {
            if (owner == null) return false;
            return owner.getUniqueId().equals(ownerUuid(entity, metaKey));
        }
    }
}
