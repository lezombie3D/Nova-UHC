package net.novaproject.novauhc.listener;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.task.ScatterTask;
import net.novaproject.novauhc.utils.UHCUtils.GeoUtils;
import net.novaproject.novauhc.utils.chat.ChatManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class PlayerMiscListeners {

    private PlayerMiscListeners() {
    }

    public static class PickupListener implements Listener {

        @EventHandler
        public void onItemPickup(PlayerPickupItemEvent event) {
            Player player = event.getPlayer();
            Item item = event.getItem();
            ScenarioManager.get().getActiveScenarios().forEach(scenario -> scenario.onPickUp(player, item, event));
        }
    }

    public static class FoodListener implements Listener {

        @EventHandler
        public void onFoodLevelChange(FoodLevelChangeEvent event) {
            if (UHCManager.get().isLobby()) {
                event.setCancelled(true);
                event.setFoodLevel(20);
                return;
            }

            ScenarioManager.get().getActiveScenarios().forEach(scenario -> scenario.noFood(event));
        }
    }

    public static class PlayerDeathListener implements Listener {
        @EventHandler
        public void onDeath(PlayerDeathEvent event) {

            if (UHCManager.get().isLobby())
                return;

            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(event.getEntity());
            UHCPlayer uhcKiller;
            if (event.getEntity().getKiller() != null) {
                uhcKiller = UHCPlayerManager.get().getPlayer(event.getEntity().getKiller());
            } else {
                uhcKiller = null;
            }

            uhcPlayer.onDeath(uhcKiller, event);

        }
    }

    public static class AntiSuffocationListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onSuffocation(EntityDamageEvent event) {
            if (event.getCause() != DamageCause.SUFFOCATION) return;
            if (!(event.getEntity() instanceof Player player)) return;
            if (!UHCManager.get().isGame()) return;

            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (uhcPlayer == null || !uhcPlayer.isPlaying()) return;
            if (!UHCManager.get().isAntiObsiTrap()) return;

            Material eyeBlock = player.getEyeLocation().getBlock().getType();
            Material feetBlock = player.getLocation().getBlock().getType();
            if (eyeBlock != Material.OBSIDIAN && feetBlock != Material.OBSIDIAN) return;

            if (GeoUtils.isOutsideBorder(player.getLocation())) return;

            event.setCancelled(true);
        }
    }

    public static class PlayerTakeDamage implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onPlayerTakeDamage(EntityDamageEvent event){

            if (UHCManager.get().isLobby()) {
                event.setCancelled(true);
                return;
            }

            Entity entity = event.getEntity();

            if (!(entity instanceof Player)) {
                return;
            }

            int timer = UHCManager.get().getTimer();

            if (timer < UHCManager.get().getInvulnerabilityDuration()) {
                event.setCancelled(true);
                return;
            }

            ScenarioManager.get().getActiveScenarios().forEach(scenario -> {

                scenario.onPlayerTakeDamage(entity, event);

            });
        }

    }

    public static class ChatListener implements Listener {

        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            ChatManager.get().handle(event);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onChatMentions(AsyncPlayerChatEvent event) {
            String lower = event.getMessage().toLowerCase(Locale.ROOT);
            if (!lower.contains("@")) return;

            Player sender = event.getPlayer();
            for (Player target : event.getRecipients()) {
                if (target == null || target.equals(sender)) continue;
                String mention = "(?<![\\w@])@" + Pattern.quote(target.getName()) + "(?![\\w])";
                if (!Pattern.compile(mention, Pattern.CASE_INSENSITIVE)
                        .matcher(event.getMessage()).find()) continue;
                Bukkit.getScheduler().runTask(Main.get(), () -> {
                    if (!target.isOnline()) return;
                    target.playSound(target.getLocation(), Sound.ORB_PICKUP, 1f, 2f);
                    DisplayService.actionBar(target,
                            LangManager.get().get(CoreLang.COMMON_MENTIONED_BY, target,
                                    Map.of("%player%", sender.getName())));
                });
            }
        }
    }

    public static class GoldenAppleListener implements Listener {

        private static final int VANILLA_ABSORPTION_HEARTS = 4;
        private static final int VANILLA_REGEN_LEVEL = 2;
        private static final int VANILLA_REGEN_HEARTS = 4;
        private static final int ABSORPTION_DURATION_TICKS = 2400;

        @EventHandler(ignoreCancelled = true)
        public void onConsume(PlayerItemConsumeEvent event) {
            if (event.getItem() == null || event.getItem().getType() != Material.GOLDEN_APPLE) return;
            if (event.getItem().getDurability() != 0) return;

            Player player = event.getPlayer();
            UHCPlayer up = UHCPlayerManager.get().getPlayer(player);
            if (up == null || !up.isPlaying()) return;
            Role role = KPIBuilder.roleOf(up);
            if (role == null) return;

            int absorptionHearts = role.getGoldenAppleAbsorptionHearts();
            int regenLevel = role.getGoldenAppleRegenLevel();
            int regenHearts = role.getGoldenAppleRegenHearts();

            boolean customAbsorption = absorptionHearts != VANILLA_ABSORPTION_HEARTS;
            boolean customRegen = regenLevel != VANILLA_REGEN_LEVEL || regenHearts != VANILLA_REGEN_HEARTS;
            if (!customAbsorption && !customRegen) return;

            Bukkit.getScheduler().runTask(Main.get(), () -> {
                if (!player.isOnline()) return;

                if (customAbsorption) {
                    player.removePotionEffect(PotionEffectType.ABSORPTION);
                    if (absorptionHearts > 0) {
                        int amplifier = Math.max(0, (absorptionHearts + 3) / 4 - 1);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,
                                ABSORPTION_DURATION_TICKS, amplifier, false, true), true);
                        PlayerUtils.setAbsorptionHearts(player, absorptionHearts);
                    }
                }

                if (customRegen) {
                    player.removePotionEffect(PotionEffectType.REGENERATION);
                    if (regenLevel > 0 && regenHearts > 0) {
                        int interval = Math.max(1, 50 / (int) Math.pow(2, regenLevel - 1));
                        int duration = Math.max(interval, (regenHearts - 1) * interval);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                                duration, regenLevel - 1, false, true), true);
                    }
                }
            });
        }
    }

    public static class PortalListener implements Listener {

        private static final int PORTAL_SEARCH_RADIUS = 20;

        @EventHandler
        public void onPlayerPortal(PlayerPortalEvent event) {
            ScenarioManager.get().getActiveScenarios().forEach(scenario -> scenario.onPortal(event));
            if (event.isCancelled()) return;

            Player player = event.getPlayer();
            String fromWorld = player.getWorld().getName();
            String arenaName = Common.get().getArenaName();

            if (fromWorld.equalsIgnoreCase("arena")) {
                linkPortal(event, player, Bukkit.getWorld(arenaName + "_nether"), 1.0 / 8.0);
                return;
            }

            if (fromWorld.equalsIgnoreCase(arenaName + "_nether")) {
                linkPortal(event, player, Common.get().getArena(), 8.0);
                return;
            }

            if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                World endWorld = Bukkit.getWorld(arenaName + "_the_end");
                if (endWorld == null) {
                    denyPortal(event, player);
                    return;
                }
                event.setTo(endWorld.getSpawnLocation());
            }
        }

        private void linkPortal(PlayerPortalEvent event, Player player, World target, double scale) {
            if (target == null) {
                denyPortal(event, player);
                return;
            }
            Location scaled = new Location(target,
                    event.getFrom().getX() * scale,
                    event.getFrom().getY(),
                    event.getFrom().getZ() * scale);
            player.teleport(event.getPortalTravelAgent().setSearchRadius(PORTAL_SEARCH_RADIUS).findOrCreate(scaled));
        }

        private void denyPortal(PlayerPortalEvent event, Player player) {
            LangManager.get().send(CoreLang.COMMON_DIM_NOT_ACCEIBLE, player);
            event.setCancelled(true);
        }
    }

    public static class MovementListener implements Listener {

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            Location loc = player.getLocation();

            if (UHCManager.get().getGameState() == UHCManager.GameState.SCATTERING
                    && ScatterTask.scattered.contains(player.getUniqueId())) {
                Location from = event.getFrom();
                Location to = event.getTo();
                if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
                    Location stay = to.clone();
                    stay.setX(from.getX());
                    stay.setZ(from.getZ());
                    event.setTo(stay);
                }
                return;
            }

            if (UHCManager.get().isLobby()) {
                if (loc.getBlockY() < 0) {
                    player.teleport(Common.get().getLobbySpawn());
                }
            }
            ScenarioManager.get().getActiveScenarios().forEach(scenario -> scenario.onMove(player, event));

            if (player.getGameMode() == GameMode.SPECTATOR
                    && (loc.getBlockY() < 0 || GeoUtils.isOutsideBorder(loc))) {
                player.teleport(new Location(player.getWorld(), 0.5, 100, 0.5));
            }
        }
    }
}
