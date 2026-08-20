package net.novaproject.novauhc.debug;

import net.novaproject.novauhc.world.generation.UHCWorldSettings;
import net.novaproject.novauhc.world.generation.BoostedRavineGenerator;
import net.novaproject.novauhc.world.generation.BoostedCaveGenerator;
import net.novaproject.novauhc.utils.schematic.SchematicStructure;
import net.novaproject.novauhc.game.VictoryManager;
import net.novaproject.novauhc.ability.Ability;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.lang.DevLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.utils.nms.MobDisguiseService;
import net.novaproject.novauhc.utils.UHCUtils.LocationRegistry;
import net.novaproject.novauhc.game.PendingDeathManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.player.utils.ReconnectionManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.cooldown.CooldownService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DevCMD extends Command {

    public DevCMD() {
        sub("log", new LogSub());
        sub("schem", DevSub.of((player, args) -> manageSchem(player)));
        sub("bypass", DevSub.of((player, args) -> {
            boolean newOp = !player.isOp();
            player.setOp(newOp);
            LangManager.get().send(DevLang.BYPASS_TOGGLED, player, Map.of("%state%", state(newOp)));
        }));
        sub("win", new WinSub());
        sub("victory", DevSub.of((player, args) -> manageVictoryDiagnostic(player)));
        sub("timer", new TimerSub());
        sub("cavestats", DevSub.of((player, args) -> manageCaveStats(player)));
        sub("orestats", DevSub.of((player, args) -> manageOreStats(player)));
        sub("loc", DevSub.of((player, args) -> manageLoc(player)));
        sub("force", new ForceSub());
        sub("forcestart", DevSub.of((player, args) -> manageForceStart(player)));
        sub("setrole", new SetRoleSub());
        sub("role", new RoleSub());
        sub("resetcd", new ResetCdSub());
        sub("regive", new RegiveSub());
        sub("pending", new PendingSub());
        sub("mobdisguise", new MobDisguiseSub());
    }

    @Override
    public void execute(CommandArguments args) {
        CommandSender sender = args.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LangManager.get().get(CoreLang.CMD_PLAYERS_ONLY));
            return;
        }
        if (!DebugLog.devMode() && !isHost(player)) {
            LangManager.get().send(CoreLang.CMD_NO_PERMISSION, player);
            return;
        }
        if (dispatchSub(args)) return;
        sendHelp(player);
    }

    private static String state(boolean on) {
        return LangManager.get().get(on ? DevLang.STATE_ON : DevLang.STATE_OFF);
    }

    private abstract static class DevSub extends Command {

        static DevSub of(BiConsumer<Player, CommandArguments> action) {
            return new DevSub() {
                @Override protected void run(Player player, CommandArguments args) { action.accept(player, args); }
            };
        }

        @Override
        public void execute(CommandArguments args) {
            if (args.getSender() instanceof Player player) run(player, args);
        }

        protected abstract void run(Player player, CommandArguments args);

        @Override
        public List<String> tabComplete(CommandArguments args) {
            return Collections.emptyList();
        }
    }

    private abstract static class TargetSub extends DevSub {

        private final String action;

        TargetSub(String action) {
            this.action = action;
        }

        @Override
        protected final void run(Player player, CommandArguments args) {
            if (args.size() < 1) {
                LangManager.get().send(DevLang.TARGET_USAGE, player, Map.of("%action%", action));
                return;
            }
            Player bukkitTarget = Bukkit.getPlayer(args.getArgument(0));
            UHCPlayer target = bukkitTarget == null ? null : UHCPlayerManager.get().getPlayer(bukkitTarget);
            if (target == null) {
                LangManager.get().send(CoreLang.COMMON_PLAYER_NOT_FOUND, player);
                return;
            }
            run(player, bukkitTarget, target, args);
        }

        protected abstract void run(Player player, Player bukkitTarget, UHCPlayer target, CommandArguments args);

        @Override
        public List<String> tabComplete(CommandArguments args) {
            return playersFirstArg(args);
        }

        protected static String rest(CommandArguments args, int from) {
            return String.join(" ", Arrays.copyOfRange(args.getArguments(), from, args.size()))
                    .replace('_', ' ').trim();
        }
    }

    private static class LogSub extends DevSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            manageLog(player, args);
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            if (args.size() == 1) {
                List<String> options = new ArrayList<>(List.of("all", "on", "off"));
                for (DebugLog.Channel channel : DebugLog.Channel.values()) options.add(channel.getKey());
                return getStrings(args, options.toArray(new String[0]));
            }
            if (args.size() == 2) return getStrings(args, "on", "off");
            return Collections.emptyList();
        }
    }

    private static class WinSub extends DevSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            VictoryManager vm = UHCManager.get().getVictoryManager();
            if (args.size() < 1) {
                LangManager.get().send(DevLang.WIN_STATUS, player, Map.of("%state%", state(vm.isVictoryEnabled())));
                return;
            }
            switch (args.getArgument(0).toLowerCase()) {
                case "on" -> {
                    vm.setVictoryEnabled(true);
                    Bukkit.broadcastMessage(LangManager.get().get(DevLang.WIN_ENABLED));
                }
                case "off" -> {
                    vm.setVictoryEnabled(false);
                    Bukkit.broadcastMessage(LangManager.get().get(DevLang.WIN_DISABLED));
                }
                default -> LangManager.get().send(DevLang.WIN_USAGE, player);
            }
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            return args.size() == 1 ? getStrings(args, "on", "off") : Collections.emptyList();
        }
    }

    private static class TimerSub extends DevSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            if (args.size() < 1) {
                LangManager.get().send(DevLang.TIMER_USAGE, player);
                return;
            }
            try {
                int sec = Integer.parseInt(args.getArgument(0));
                UHCManager.get().setTimer(sec);
                LangManager.get().send(DevLang.TIMER_SET, player,
                        Map.of("%seconds%", sec, "%formatted%", UHCManager.get().getTimerFormatted()));
            } catch (NumberFormatException e) {
                LangManager.get().send(DevLang.INVALID_VALUE, player);
            }
        }
    }

    private static class ForceSub extends DevSub {

        @Override
        protected void run(Player player, CommandArguments args) {
            manageForce(player, args);
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            return args.size() == 1 ? getStrings(args, TimedTriggers.names().toArray(new String[0])) : Collections.emptyList();
        }
    }

    private static class SetRoleSub extends TargetSub {

        SetRoleSub() {
            super("setrole");
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected void run(Player player, Player bukkitTarget, UHCPlayer target, CommandArguments args) {
            ScenarioRole scenarioRole = KPIBuilder.activeScenarioRole();
            if (scenarioRole == null) {
                LangManager.get().send(DevLang.NO_ROLEMODE, player);
                return;
            }
            if (args.size() < 2) {
                LangManager.get().send(DevLang.SETROLE_USAGE, player);
                return;
            }
            String want = rest(args, 1);
            Role match = null;
            for (Object obj : scenarioRole.getRoleConfigs().values()) {
                Role config = (Role) obj;
                String plain = ChatColor.stripColor(config.getName());
                if (plain.equalsIgnoreCase(want) || config.getClass().getSimpleName().equalsIgnoreCase(want)) {
                    match = config;
                    break;
                }
                if (match == null && plain.toLowerCase().startsWith(want.toLowerCase())) {
                    match = config;
                }
            }
            if (match == null) {
                LangManager.get().send(DevLang.ROLE_NOT_FOUND, player, Map.of("%role%", want));
                return;
            }
            scenarioRole.setRole(target, match.clone());
            Role given = KPIBuilder.roleOf(target);
            if (given != null) {
                if (given.getKnowPlayers().isEmpty()) given.registerKnowPlayers();
                given.sendKnowPlayers();
            }
            LangManager.get().send(DevLang.SETROLE_DONE, player, Map.of(
                    "%role%", match.getColor() + ChatColor.stripColor(match.getName()),
                    "%player%", bukkitTarget.getName(),
                    "%extra%", LangManager.get().get(DevLang.SETROLE_EXTRA_REVEAL)));
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            if (args.size() == 1) return getOnlinePlayers(args);
            if (args.size() != 2) return Collections.emptyList();
            ScenarioRole<?> scenarioRole = KPIBuilder.activeScenarioRole();
            if (scenarioRole == null) return Collections.emptyList();
            List<String> names = new ArrayList<>();
            for (Object obj : scenarioRole.getRoleConfigs().values()) {
                names.add(ChatColor.stripColor(((Role) obj).getName()).replace(' ', '_'));
            }
            Collections.sort(names);
            return getStrings(args, names.toArray(new String[0]));
        }
    }

    private static class RoleSub extends TargetSub {

        RoleSub() {
            super("role");
        }

        @Override
        protected void run(Player player, Player bukkitTarget, UHCPlayer target, CommandArguments args) {
            Role role = KPIBuilder.roleOf(target);
            LangManager.get().send(DevLang.ROLE_HEADER, player, Map.of("%player%", bukkitTarget.getName()));
            LangManager.get().send(DevLang.ROLE_PLAYING, player, Map.of(
                    "%playing%", target.isPlaying(),
                    "%pending%", PendingDeathManager.get().isPending(bukkitTarget.getUniqueId()),
                    "%spec%", target.isSpec()));
            if (role == null) {
                LangManager.get().send(DevLang.ROLE_NONE, player);
                return;
            }
            LangManager.get().send(DevLang.ROLE_LINE, player, Map.of(
                    "%role%", role.getName(),
                    "%camp%", role.getCamp() == null ? "?" : role.getCamp().getColor() + role.getCamp().getName()));
            if (role.getPartnerUuid() != null) {
                OfflinePlayer partner = Bukkit.getOfflinePlayer(role.getPartnerUuid());
                LangManager.get().send(DevLang.ROLE_PARTNER, player, Map.of("%partner%",
                        partner != null && partner.getName() != null ? partner.getName() : role.getPartnerUuid()));
            }
            LangManager.get().send(DevLang.ROLE_ABILITIES, player, Map.of("%count%", role.getAbilities().size()));
            for (Ability ability : role.getAbilities()) {
                long cd = CooldownService.get(bukkitTarget, ability.getName() + "Cooldown");
                String cdTxt = cd <= 0 ? "§aprêt" : "§c" + ((cd + 999) / 1000) + "s";
                String flags = !ability.isUsable() ? " §8(§cdisabled§8)" : "";
                LangManager.get().send(DevLang.ROLE_ABILITY_LINE, player, Map.of(
                        "%ability%", ability.getName(), "%cd%", cdTxt,
                        "%uses%", ability.getMaxUse(), "%flags%", flags));
            }
        }
    }

    private static class ResetCdSub extends TargetSub {

        ResetCdSub() {
            super("resetcd");
        }

        @Override
        protected void run(Player player, Player bukkitTarget, UHCPlayer target, CommandArguments args) {
            if (args.size() >= 2) {
                String name = rest(args, 1);
                long left = CooldownService.remove(bukkitTarget, name + "Cooldown");
                if (left > 0) {
                    LangManager.get().send(DevLang.RESETCD_ONE, player,
                            Map.of("%ability%", name, "%seconds%", (left + 999) / 1000));
                } else {
                    LangManager.get().send(DevLang.RESETCD_NONE, player, Map.of("%ability%", name));
                }
                return;
            }
            int cleared = CooldownService.clearAll(bukkitTarget);
            LangManager.get().send(DevLang.RESETCD_DONE, player,
                    Map.of("%count%", cleared, "%player%", bukkitTarget.getName()));
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            if (args.size() == 1) return getOnlinePlayers(args);
            if (args.size() != 2) return Collections.emptyList();
            Player target = Bukkit.getPlayer(args.getArgument(0));
            if (target == null) return Collections.emptyList();
            List<String> keys = new ArrayList<>();
            for (String key : CooldownService.getActiveKeys(target)) {
                if (key.endsWith("Cooldown")) {
                    keys.add(key.substring(0, key.length() - "Cooldown".length()).replace(' ', '_'));
                }
            }
            Collections.sort(keys);
            return getStrings(args, keys.toArray(new String[0]));
        }
    }

    private static class RegiveSub extends TargetSub {

        RegiveSub() {
            super("regive");
        }

        @Override
        protected void run(Player player, Player bukkitTarget, UHCPlayer target, CommandArguments args) {
            Role role = KPIBuilder.roleOf(target);
            if (role == null) {
                LangManager.get().send(DevLang.NO_ROLE, player);
                return;
            }
            role.onGive(target);
            if (role.getKnowPlayers().isEmpty()) role.registerKnowPlayers();
            role.sendKnowPlayers();
            LangManager.get().send(DevLang.REGIVE_DONE, player, Map.of(
                    "%player%", bukkitTarget.getName(),
                    "%extra%", " §7(" + role.getAbilities().size() + " abilities)"));
        }
    }

    private static class PendingSub extends TargetSub {

        PendingSub() {
            super("pending");
        }

        @Override
        protected void run(Player player, Player bukkitTarget, UHCPlayer target, CommandArguments args) {
            PendingDeathManager pdm = PendingDeathManager.get();
            if (!pdm.isPending(bukkitTarget.getUniqueId())) {
                LangManager.get().send(DevLang.NOT_PENDING, player, Map.of("%player%", bukkitTarget.getName()));
                return;
            }
            if (args.get(1, "cancel").equalsIgnoreCase("finalize")) {
                pdm.forceImmediateDeath(target);
                LangManager.get().send(DevLang.PENDING_FINALIZED, player, Map.of("%player%", bukkitTarget.getName()));
            } else {
                pdm.cancelPendingDeath(target);
                LangManager.get().send(DevLang.PENDING_CANCELLED, player, Map.of("%player%", bukkitTarget.getName()));
            }
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            if (args.size() == 1) return getOnlinePlayers(args);
            return args.size() == 2 ? getStrings(args, "cancel", "finalize") : Collections.emptyList();
        }
    }

    private static class MobDisguiseSub extends TargetSub {

        MobDisguiseSub() {
            super("mobdisguise");
        }

        @Override
        protected void run(Player player, Player bukkitTarget, UHCPlayer target, CommandArguments args) {
            if (args.size() < 2) {
                LangManager.get().send(MOBDISGUISE_USAGE, player);
                return;
            }
            String want = args.getArgument(1);
            if (want.equalsIgnoreCase("off")) {
                MobDisguiseService.get().undisguise(bukkitTarget);
                LangManager.get().send(MOBDISGUISE_OFF, player, Map.of("%player%", bukkitTarget.getName()));
                return;
            }
            EntityType type = livingType(want);
            if (type == null) {
                LangManager.get().send(MOBDISGUISE_INVALID, player, Map.of("%type%", want));
                return;
            }
            MobDisguiseService.get().disguise(bukkitTarget, type);
            LangManager.get().send(MOBDISGUISE_DONE, player,
                    Map.of("%player%", bukkitTarget.getName(), "%type%", type.name().toLowerCase()));
        }

        @Override
        public List<String> tabComplete(CommandArguments args) {
            if (args.size() == 1) return getOnlinePlayers(args);
            if (args.size() != 2) return Collections.emptyList();
            List<String> names = new ArrayList<>(List.of("off"));
            for (EntityType type : EntityType.values()) {
                if (isDisguisable(type)) names.add(type.name().toLowerCase());
            }
            Collections.sort(names);
            return getStrings(args, names.toArray(new String[0]));
        }

        private static EntityType livingType(String name) {
            for (EntityType type : EntityType.values()) {
                if (isDisguisable(type) && type.name().equalsIgnoreCase(name)) return type;
            }
            return null;
        }

        private static boolean isDisguisable(EntityType type) {
            return MobDisguiseService.get().supports(type);
        }
    }

    private static final DynamicLang MOBDISGUISE_USAGE =
            DynamicLang.of("dev.MOBDISGUISE_USAGE", "§c✖ §7Usage: §f/dev mobdisguise <joueur> <type|off>");
    private static final DynamicLang MOBDISGUISE_INVALID =
            DynamicLang.of("dev.MOBDISGUISE_INVALID", "§c✖ §7Mob inconnu : §f%type%§7.");
    private static final DynamicLang MOBDISGUISE_DONE =
            DynamicLang.of("dev.MOBDISGUISE_DONE", "§a✔ §f%player% §7est déguisé en §f%type%§7.");
    private static final DynamicLang MOBDISGUISE_OFF =
            DynamicLang.of("dev.MOBDISGUISE_OFF", "§a✔ §7Déguisement de §f%player% §7retiré.");
    private static final DynamicLang HELP_MOBDISGUISE =
            DynamicLang.of("dev.HELP_MOBDISGUISE",
                    "§6▸ §e/dev mobdisguise <joueur> <type|off> §7— déguise un joueur en mob pour les autres");

    private static void manageLog(Player player, CommandArguments args) {
        String[] arguments = shiftedArgs(args);
        if (arguments.length < 2) {
            LangManager.get().send(DevLang.LOG_HEADER, player);
            for (DebugLog.Channel channel : DebugLog.Channel.values()) {
                boolean on = DebugLog.isEnabled(channel);
                LangManager.get().send(DevLang.LOG_CHANNEL_LINE, player, Map.of("%channel%", channel.getKey(), "%state%", LangManager.get().get(on ? DevLang.STATE_ON : DevLang.STATE_OFF), "%description%", channel.getDescription()));
            }
            LangManager.get().send(DevLang.LOG_USAGE, player);
            return;
        }

        String first = arguments[1].toLowerCase();

        if (first.equals("on") || first.equals("off") || first.equals("all")) {
            boolean newState;
            if (first.equals("all")) {
                newState = arguments.length < 3 || arguments[2].equalsIgnoreCase("on");
            } else {
                newState = first.equals("on");
            }
            DebugLog.setAll(newState);
            LangManager.get().send(DevLang.LOG_ALL_TOGGLED, player, Map.of("%state%", LangManager.get().get(newState ? DevLang.STATE_ON : DevLang.STATE_OFF)));
            return;
        }

        DebugLog.Channel channel = DebugLog.Channel.fromKey(first);
        if (channel == null) {
            LangManager.get().send(DevLang.LOG_UNKNOWN_CHANNEL, player, Map.of("%channels%", Arrays.stream(DebugLog.Channel.values())
                            .map(DebugLog.Channel::getKey)
                            .collect(Collectors.joining(", "))));
            return;
        }

        boolean newState = arguments.length >= 3
                ? arguments[2].equalsIgnoreCase("on")
                : !DebugLog.isEnabled(channel);
        DebugLog.setEnabled(channel, newState);
        LangManager.get().send(DevLang.LOG_CHANNEL_TOGGLED, player, Map.of("%channel%", channel.getKey(), "%state%", LangManager.get().get(newState ? DevLang.STATE_ON : DevLang.STATE_OFF)));
    }

    private static void manageSchem(Player player) {
        List<SchematicStructure> placed =
                SchematicStructure.getPlaced();
        LangManager.get().send(DevLang.SCHEM_HEADER, player);
        if (placed.isEmpty()) {
            LangManager.get().send(DevLang.SCHEM_NONE, player);
            return;
        }
        for (SchematicStructure structure : placed) {
            Location anchor = structure.getAnchor();
            if (anchor == null) continue;
            SchematicStructure.Dimensions dims = structure.getDimensions();
            String dimsTxt = dims == null ? "?" : dims.width() + "x" + dims.height() + "x" + dims.length();
            String dist = anchor.getWorld() != null && anchor.getWorld().equals(player.getWorld())
                    ? String.valueOf((int) anchor.distance(player.getLocation()))
                    : "autre monde";
            LangManager.get().send(DevLang.SCHEM_LINE, player, Map.of("%file%", structure.getFileName(), "%info%", "§e" + anchor.getBlockX() + " " + anchor.getBlockY() + " " + anchor.getBlockZ()
                    + " §7(" + (anchor.getWorld() == null ? "?" : anchor.getWorld().getName()) + ")"
                    + " §8| §7dims=" + dimsTxt + " §8| §7dist=" + dist));
        }
    }

    private static String[] shiftedArgs(CommandArguments args) {
        String[] raw = args.getArguments();
        String[] out = new String[raw.length + 1];
        out[0] = "";
        System.arraycopy(raw, 0, out, 1, raw.length);
        return out;
    }

    private static void manageForce(Player player, CommandArguments args) {
        String[] arguments = shiftedArgs(args);
        if (TimedTriggers.names().isEmpty()) {
            LangManager.get().send(DevLang.FORCE_NONE, player);
            return;
        }
        if (arguments.length < 2) {
            LangManager.get().send(DevLang.FORCE_HEADER, player);
            for (String name : TimedTriggers.names()) {
                TimedTriggers.Trigger trigger = TimedTriggers.get(name);
                LangManager.get().send(DevLang.FORCE_LINE, player, Map.of("%name%", name, "%description%", trigger.description() == null ? "" : trigger.description()));
            }
            return;
        }
        String name = arguments[1];
        if (!TimedTriggers.fire(name)) {
            LangManager.get().send(DevLang.FORCE_UNKNOWN, player, Map.of("%name%", name));
            return;
        }
        LangManager.get().send(DevLang.FORCE_TRIGGERED, player, Map.of("%name%", name));
    }

    private static void sendHelp(Player player) {
        LangManager.get().send(DevLang.HELP_HEADER, player);
        LangManager.get().send(DevLang.HELP_LOG, player);
        LangManager.get().send(DevLang.HELP_SCHEM, player);
        LangManager.get().send(DevLang.HELP_BYPASS, player);
        LangManager.get().send(DevLang.HELP_SETROLE, player);
        LangManager.get().send(DevLang.HELP_ROLE, player);
        LangManager.get().send(DevLang.HELP_RESETCD, player);
        LangManager.get().send(DevLang.HELP_REGIVE, player);
        LangManager.get().send(DevLang.HELP_PENDING, player);
        LangManager.get().send(HELP_MOBDISGUISE, player);
        LangManager.get().send(DevLang.HELP_TIMER, player);
        LangManager.get().send(DevLang.HELP_WIN, player);
        LangManager.get().send(DevLang.HELP_VICTORY, player);
        LangManager.get().send(DevLang.HELP_STATS, player);
        LangManager.get().send(DevLang.HELP_LOC, player);
        LangManager.get().send(DevLang.HELP_FORCE, player);
        LangManager.get().send(DevLang.HELP_FORCESTART, player);
    }

    private static void manageForceStart(Player player) {
        UHCManager uhc = UHCManager.get();
        if (uhc.isStarted() || uhc.getGameState() != UHCManager.GameState.LOBBY) {
            LangManager.get().send(DevLang.FORCESTART_ALREADY, player,
                    Map.of("%state%", uhc.getGameState().name()));
            return;
        }
        uhc.setCanceled(false);
        uhc.onStart(true);
        uhc.setStarted(true);
        LangManager.get().send(DevLang.FORCESTART_DONE, player,
                Map.of("%players%", Bukkit.getOnlinePlayers().size()));
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return tabCompleteSub(args);
    }

    private static void manageCaveStats(Player player) {
        Location origin = player.getLocation();
        World world = origin.getWorld();
        int radius = 80;
        int yMin = 10, yMax = 50;
        long total = 0, air = 0;
        for (int dx = -radius; dx <= radius; dx += 2) {
            for (int dz = -radius; dz <= radius; dz += 2) {
                for (int y = yMin; y <= yMax; y += 2) {
                    Material t = world.getBlockAt(origin.getBlockX() + dx, y, origin.getBlockZ() + dz).getType();
                    total++;
                    if (t == Material.AIR) air++;
                }
            }
        }
        double pct = total == 0 ? 0.0 : (air * 100.0 / total);

        int caveCalls   = BoostedCaveGenerator.CHUNKS_GENERATED.get();
        long cavePasses = BoostedCaveGenerator.TOTAL_PASSES.get();
        int ravCalls    = BoostedRavineGenerator.CHUNKS_GENERATED.get();
        long ravPasses  = BoostedRavineGenerator.TOTAL_PASSES.get();
        int caveMult    = UHCManager.get().getCaveMultiplier();
        int ravMult     = UHCManager.get().getRavineMultiplier();

        LangManager.get().send(DevLang.CAVESTATS_HEADER, player);
        LangManager.get().send(DevLang.STATS_WORLD, player, Map.of("%world%", world.getName()));
        LangManager.get().send(DevLang.CAVESTATS_MULT, player, Map.of("%caves%", caveMult, "%ravines%", ravMult));
        LangManager.get().send(DevLang.CAVESTATS_CAVE_CALLS, player, Map.of("%chunks%", caveCalls, "%passes%", cavePasses));
        LangManager.get().send(DevLang.CAVESTATS_RAV_CALLS, player, Map.of("%chunks%", ravCalls, "%passes%", ravPasses));
        if (caveCalls == 0 || ravCalls == 0) {
            LangManager.get().send(DevLang.CAVESTATS_BROKEN, player);
        } else if (cavePasses < (long) caveCalls * caveMult) {
            LangManager.get().send(DevLang.CAVESTATS_ANOMALY, player);
        } else {
            LangManager.get().send(DevLang.CAVESTATS_OK, player);
        }
        LangManager.get().send(DevLang.CAVESTATS_AIR, player, Map.of("%ymin%", yMin, "%ymax%", yMax, "%size%", 2 * radius, "%pct%", String.format("%.1f", pct)));
        LangManager.get().send(DevLang.STATS_FOOTER, player);
    }

    private static void manageOreStats(Player player) {
        Location origin = player.getLocation();
        World world = origin.getWorld();
        int chunkRadius = 5;
        int cx0 = origin.getChunk().getX() - chunkRadius;
        int cz0 = origin.getChunk().getZ() - chunkRadius;
        int chunksScanned = 0;
        int diamond = 0, gold = 0, iron = 0, lapis = 0, coal = 0, redstone = 0, emerald = 0;

        for (int cx = cx0; cx <= cx0 + 2 * chunkRadius; cx++) {
            for (int cz = cz0; cz <= cz0 + 2 * chunkRadius; cz++) {
                if (!world.isChunkLoaded(cx, cz)) {
                    if (!world.loadChunk(cx, cz, false)) continue;
                }
                chunksScanned++;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 1; y < 64; y++) {
                            Material t = world.getBlockAt(cx * 16 + x, y, cz * 16 + z).getType();
                            if (t == Material.DIAMOND_ORE)  diamond++;
                            else if (t == Material.GOLD_ORE)    gold++;
                            else if (t == Material.IRON_ORE)    iron++;
                            else if (t == Material.LAPIS_ORE)   lapis++;
                            else if (t == Material.COAL_ORE)    coal++;
                            else if (t == Material.REDSTONE_ORE || t == Material.GLOWING_REDSTONE_ORE) redstone++;
                            else if (t == Material.EMERALD_ORE) emerald++;
                        }
                    }
                }
            }
        }

        double n = Math.max(1, chunksScanned);
        int dCfg = UHCWorldSettings.diamondCount();
        int gCfg = UHCWorldSettings.goldCount();
        int iCfg = UHCWorldSettings.ironCount();
        int lCfg = UHCWorldSettings.lapisCount();
        double dMult = UHCWorldSettings.diamondMultiplier();
        double gMult = UHCWorldSettings.goldMultiplier();
        double iMult = UHCWorldSettings.ironMultiplier();
        double lMult = UHCWorldSettings.lapisMultiplier();

        LangManager.get().send(DevLang.ORESTATS_HEADER, player);
        LangManager.get().send(DevLang.ORESTATS_SCAN, player, Map.of("%world%", world.getName(), "%chunks%", chunksScanned));
        LangManager.get().send(DevLang.ORESTATS_CONFIG_HEADER, player);
        LangManager.get().send(DevLang.ORESTATS_CONFIG_LINE, player, Map.of("%ore%", "§bDiamond", "%cfg%", dCfg, "%mult%", String.format("%.2f", dMult), "%boost%", String.format("%.2f", UHCManager.get().getBoost_Diamond())));
        LangManager.get().send(DevLang.ORESTATS_CONFIG_LINE, player, Map.of("%ore%", "§6Gold", "%cfg%", gCfg, "%mult%", String.format("%.2f", gMult), "%boost%", String.format("%.2f", UHCManager.get().getBoost_Gold())));
        LangManager.get().send(DevLang.ORESTATS_CONFIG_LINE, player, Map.of("%ore%", "§fIron", "%cfg%", iCfg, "%mult%", String.format("%.2f", iMult), "%boost%", String.format("%.2f", UHCManager.get().getBoost_Iron())));
        LangManager.get().send(DevLang.ORESTATS_CONFIG_LINE, player, Map.of("%ore%", "§9Lapis", "%cfg%", lCfg, "%mult%", String.format("%.2f", lMult), "%boost%", String.format("%.2f", UHCManager.get().getBoost_Lapis())));
        LangManager.get().send(DevLang.ORESTATS_MEASURED_HEADER, player);
        LangManager.get().send(DevLang.ORESTATS_MEASURED_LINE, player, Map.of("%ore%", "§bDiamond", "%value%", String.format("%.2f", diamond / n), "%ref%", "vanilla ≈ 3.7"));
        LangManager.get().send(DevLang.ORESTATS_MEASURED_LINE, player, Map.of("%ore%", "§6Gold", "%value%", String.format("%.2f", gold / n), "%ref%", "vanilla ≈ 8.5"));
        LangManager.get().send(DevLang.ORESTATS_MEASURED_LINE, player, Map.of("%ore%", "§fIron", "%value%", String.format("%.2f", iron / n), "%ref%", "vanilla ≈ 77"));
        LangManager.get().send(DevLang.ORESTATS_MEASURED_LINE, player, Map.of("%ore%", "§9Lapis", "%value%", String.format("%.2f", lapis / n), "%ref%", "vanilla ≈ 3.5"));
        LangManager.get().send(DevLang.ORESTATS_MEASURED_LINE, player, Map.of("%ore%", "§8Coal", "%value%", String.format("%.2f", coal / n), "%ref%", "vanilla, non boosté"));
        LangManager.get().send(DevLang.ORESTATS_MEASURED_LINE, player, Map.of("%ore%", "§cRedstone", "%value%", String.format("%.2f", redstone / n), "%ref%", "vanilla"));
        LangManager.get().send(DevLang.ORESTATS_MEASURED_LINE, player, Map.of("%ore%", "§aEmerald", "%value%", String.format("%.2f", emerald / n), "%ref%", "vanilla, biome-dépendant"));
        LangManager.get().send(DevLang.STATS_FOOTER, player);
    }

    private static void manageVictoryDiagnostic(Player player) {
        LangManager.get().send(DevLang.VICTORY_HEADER, player);
        VictoryManager vm = UHCManager.get().getVictoryManager();
        LangManager.get().send(DevLang.VICTORY_ENABLED, player, Map.of("%state%", LangManager.get().get(vm.isVictoryEnabled() ? DevLang.VICTORY_STATE_YES : DevLang.VICTORY_STATE_NO)));
        LangManager.get().send(DevLang.VICTORY_GAMESTATE, player, Map.of("%state%", UHCManager.get().getGameState()));

        PendingDeathManager pdm = PendingDeathManager.get();
        if (pdm.hasAnyPending()) {
            LangManager.get().send(DevLang.VICTORY_PENDING_HEADER, player);
            for (UUID uuid : pdm.getPendingUuids()) {
                OfflinePlayer pending = Bukkit.getOfflinePlayer(uuid);
                String name = pending.getName() != null ? pending.getName() : uuid.toString().substring(0, 8);
                LangManager.get().send(DevLang.VICTORY_PENDING_LINE, player, Map.of("%player%", name, "%seconds%", pdm.getRemainingSeconds(uuid)));
            }
        } else {
            LangManager.get().send(DevLang.VICTORY_PENDING_NONE, player);
        }

        if (ReconnectionManager.get() != null && ReconnectionManager.get().hasAnyPendingReconnection()) {
            LangManager.get().send(DevLang.VICTORY_RECONNECT_WAITING, player);
        } else {
            LangManager.get().send(DevLang.VICTORY_RECONNECT_NONE, player);
        }

        ScenarioRole<?> scenarioRole = KPIBuilder.activeScenarioRole();
        if (scenarioRole == null) {
            LangManager.get().send(DevLang.VICTORY_NO_ROLEMODE, player);
            return;
        }
        boolean custom = scenarioRole.hasCustomWinCondition();
        LangManager.get().send(DevLang.VICTORY_CUSTOM, player, Map.of("%state%", LangManager.get().get(custom ? DevLang.VICTORY_CUSTOM_YES : DevLang.VICTORY_CUSTOM_NO)));
        if (!scenarioRole.isRolesDistributed()) {
            LangManager.get().send(DevLang.VICTORY_NOT_DISTRIBUTED, player);
            return;
        }

        Map<String, List<String>> groups = new LinkedHashMap<>();
        List<String> ignored = new ArrayList<>();
        for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            String name = up.getPlayer() != null ? up.getPlayer().getName() : up.getUuid().toString().substring(0, 8);
            Role role = scenarioRole.getRoleByUHCPlayer(up);
            if (role == null || role.getCamp() == null) {
                ignored.add(name);
                continue;
            }
            groups.computeIfAbsent(scenarioRole.winGroupKey(up, role), k -> new ArrayList<>()).add(name);
        }
        if (custom) {
            LangManager.get().send(DevLang.VICTORY_GROUPS_CUSTOM, player, Map.of("%count%", groups.size()));
        } else {
            LangManager.get().send(DevLang.VICTORY_GROUPS, player, Map.of("%count%", groups.size()));
        }
        groups.forEach((key, names) ->
                LangManager.get().send(DevLang.VICTORY_GROUP_LINE, player, Map.of("%key%", key, "%players%", String.join("§7, §f", names))));
        if (!ignored.isEmpty()) {
            LangManager.get().send(DevLang.VICTORY_IGNORED, player, Map.of("%players%", String.join(", ", ignored)));
        }
    }

    private static void manageLoc(Player player) {
        List<LocationRegistry.Entry> entries = LocationRegistry.getEntries();
        LangManager.get().send(DevLang.LOC_HEADER, player);
        if (entries.isEmpty()) {
            LangManager.get().send(DevLang.LOC_NONE, player);
            return;
        }

        Map<String, List<LocationRegistry.Entry>> byCat = new LinkedHashMap<>();
        for (LocationRegistry.Entry e : entries) {
            byCat.computeIfAbsent(e.category(), k -> new ArrayList<>()).add(e);
        }
        for (var catEntry : byCat.entrySet()) {
            LangManager.get().send(DevLang.LOC_CATEGORY, player, Map.of("%category%", catEntry.getKey(), "%count%", catEntry.getValue().size()));
            for (LocationRegistry.Entry e : catEntry.getValue()) {
                Location loc = e.location();
                String dist = (loc.getWorld() != null && loc.getWorld().equals(player.getWorld()))
                        ? " §8| §7dist=" + (int) loc.distance(player.getLocation())
                        : "";
                LangManager.get().send(DevLang.LOC_LINE, player, Map.of("%name%", e.name(), "%coords%", loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()
                        + " §7(" + (loc.getWorld() == null ? "?" : loc.getWorld().getName()) + ")"
                        + dist));
            }
        }
    }
}