package net.novaproject.novauhc.lang;

import net.novaproject.novauhc.debug.DebugLog;
import net.novaproject.novauhc.utils.UHCUtils.GeoUtils;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.listener.PlayerConnectionEvent;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.UHCUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.configuration.InvalidConfigurationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Location;

public class LangManager implements Lang.ILangManager {

    private static LangManager instance;

    public static LangManager get() {
        return instance;
    }

    private final File langFolder;
    private final String serverDefaultLocale;
    private static final String LINE_BREAK = "\n";

    private static final String LINES_SUFFIX = "_LINES";

    private final Map<String, Map<String, List<String>>> translations = new ConcurrentHashMap<>();
    private final List<Lang> registered = new ArrayList<>();
    private boolean reloadRequested = false;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LangManager(File dataFolder, String serverDefaultLocale) {
        this.langFolder = new File(dataFolder, "lang");
        this.serverDefaultLocale = serverDefaultLocale;
        instance = this;
    }

    public void register(Lang[] values) {
        registered.addAll(Arrays.asList(values));
    }

    public void generateAndLoad() {
        registered.addAll(DynamicLang.drainPending());
        if (!langFolder.exists()) langFolder.mkdirs();

        Set<String> locales = new LinkedHashSet<>();
        locales.add(serverDefaultLocale);
        File[] existing = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (existing != null) {
            for (File f : existing) {
                String code = f.getName().substring(0, f.getName().length() - 4);
                if (isValidLocaleCode(code)) locales.add(code);
            }
        }

        for (String locale : locales) {
            File file = new File(langFolder, locale + ".yml");
            YamlConfiguration yaml = new YamlConfiguration();
            if (file.exists()) {
                try {
                    yaml.loadFromString(sanitizeYaml(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)));
                } catch (IOException | InvalidConfigurationException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "[LangManager] Impossible de lire " + file.getName(), e);
                }
            }

            boolean dirty = migrateNumberedKeys(yaml);
            for (Lang lang : registered) {
                String key = lang.getKey();
                if (!yaml.contains(key)) {
                    String msg = lang.getTranslations().getOrDefault(locale, lang.getFallback());
                    if (msg.contains(LINE_BREAK)) yaml.set(key, Arrays.asList(msg.split(LINE_BREAK, -1)));
                    else yaml.set(key, msg);
                    dirty = true;
                }
            }

            if (dirty || !file.exists()) {
                try {
                    Files.write(file.toPath(), yaml.saveToString().getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "[LangManager] Impossible de sauvegarder " + file.getName(), e);
                }
            }

            Map<String, List<String>> map = new HashMap<>();
            for (String key : yaml.getKeys(true)) {
                if (yaml.isConfigurationSection(key)) continue;
                List<String> lines = new ArrayList<>();
                if (yaml.isList(key)) {
                    for (String line : yaml.getStringList(key)) {
                        lines.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                } else {
                    lines.add(ChatColor.translateAlternateColorCodes('&', yaml.getString(key, "")));
                }
                map.put(key, lines);
            }
            translations.put(locale, map);
        }

        Bukkit.getLogger().info("[LangManager] Chargé. Locales : " + locales + " | Clés : " + registered.size());
    }

    private boolean migrateNumberedKeys(YamlConfiguration yaml) {
        boolean dirty = false;
        for (Lang lang : registered) {
            String key = lang.getKey();
            if (!key.endsWith(LINES_SUFFIX) || yaml.contains(key)) continue;

            String base = key.substring(0, key.length() - LINES_SUFFIX.length());
            List<String> lines = new ArrayList<>();
            List<String> oldKeys = new ArrayList<>();
            for (int i = 1; ; i++) {
                String oldKey = base + "_L" + i;
                if (!yaml.contains(oldKey)) break;
                lines.add(yaml.getString(oldKey, ""));
                oldKeys.add(oldKey);
            }
            if (lines.isEmpty()) continue;

            yaml.set(key, lines);
            for (String oldKey : oldKeys) yaml.set(oldKey, null);
            dirty = true;
            Bukkit.getLogger().info("[LangManager] " + key + " : " + oldKeys.size()
                    + " lignes numerotees migrees en liste.");
        }
        return dirty;
    }

    private boolean isValidLocaleCode(String code) {
        return code.matches("[a-z]{2}(_[A-Z]{2})?");
    }

    private static String sanitizeYaml(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int v = c;
            boolean printable = v == 0x09 || v == 0x0A || v == 0x0D || v == 0x85
                    || (v >= 0x20 && v <= 0x7E)
                    || (v >= 0xA0 && v <= 0xD7FF)
                    || (v >= 0xE000 && v <= 0xFFFD);
            if (printable) sb.append(c);
        }
        return sb.toString();
    }

    public void requestReload() {
        Main main = Main.get();
        if (main == null) { generateAndLoad(); return; }
        if (reloadRequested) return;
        reloadRequested = true;
        Bukkit.getScheduler().runTask(main, () -> {
            reloadRequested = false;
            generateAndLoad();
        });
    }

    public void importShipped(JavaPlugin module) {
        if (!langFolder.exists()) langFolder.mkdirs();
        for (String code : readShippedIndex(module)) {
            YamlConfiguration frag = loadResourceYaml(module, "lang/" + code + ".yml");
            if (frag == null) continue;

            File file = new File(langFolder, code + ".yml");
            YamlConfiguration yaml = new YamlConfiguration();
            if (file.exists()) {
                try {
                    yaml.loadFromString(sanitizeYaml(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)));
                } catch (IOException | InvalidConfigurationException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "[LangManager] Impossible de lire " + file.getName(), e);
                }
            }

            boolean dirty = false;
            for (String key : frag.getKeys(true)) {
                if (frag.isConfigurationSection(key)) continue;
                if (!yaml.contains(key)) {
                    yaml.set(key, frag.isList(key) ? frag.getStringList(key) : frag.getString(key));
                    dirty = true;
                }
            }

            if (dirty || !file.exists()) {
                try {
                    Files.write(file.toPath(), yaml.saveToString().getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "[LangManager] Impossible de sauvegarder " + file.getName(), e);
                }
            }
        }
    }

    private List<String> readShippedIndex(JavaPlugin module) {
        List<String> codes = new ArrayList<>();
        try (InputStream in = module.getResource("lang/_shipped.txt")) {
            if (in == null) return codes;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                String code = line.trim();
                if (!code.isEmpty() && isValidLocaleCode(code)) codes.add(code);
            }
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[LangManager] Impossible de lire lang/_shipped.txt de " + module.getName(), e);
        }
        return codes;
    }

    private YamlConfiguration loadResourceYaml(JavaPlugin module, String resourcePath) {
        try (InputStream in = module.getResource(resourcePath)) {
            if (in == null) return null;
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(sanitizeYaml(new String(in.readAllBytes(), StandardCharsets.UTF_8)));
            return yaml;
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[LangManager] Impossible de lire la ressource " + resourcePath + " de " + module.getName(), e);
            return null;
        }
    }

    public String getRaw(Lang key, String locale) {
        return String.join(LINE_BREAK, getRawLines(key, locale));
    }

    public List<String> getRawLines(Lang key, String locale) {
        Map<String, List<String>> map = translations.get(locale);
        if (map != null && map.containsKey(key.getKey())) {
            return map.get(key.getKey());
        }
        Map<String, List<String>> def = translations.get(serverDefaultLocale);
        if (def != null && def.containsKey(key.getKey())) {
            return def.get(key.getKey());
        }
        return Arrays.asList(key.getFallback().split(LINE_BREAK, -1));
    }

    public String get(Lang key, Player player, Map<String, Object> extra) {
        if (player == null || !player.isOnline()) {
            String msg = getRaw(key, serverDefaultLocale);
            return applyPlaceholders(msg, buildPlaceholders(null, extra));
        }

        UHCPlayer uhc = UHCPlayerManager.get() != null ? UHCPlayerManager.get().getPlayer(player) : null;
        String locale = (uhc != null) ? uhc.getLocale() : serverDefaultLocale;
        String msg = getRaw(key, locale);
        return applyPlaceholders(msg, buildPlaceholders(uhc, extra));
    }

    public String get(Lang key, Player player) {
        return get(key, player, null);
    }

    public String get(Lang key, Map<String, Object> extra) {
        String msg = getRaw(key, serverDefaultLocale);
        return applyPlaceholders(msg, buildPlaceholders(null, extra));
    }

    public String get(Lang key) {
        return get(key, (Map<String, Object>) null);
    }

    public List<String> getLines(Lang key, Player player, Map<String, Object> extra) {
        UHCPlayer uhc = player != null && player.isOnline() && UHCPlayerManager.get() != null
                ? UHCPlayerManager.get().getPlayer(player) : null;
        String locale = uhc != null ? uhc.getLocale() : serverDefaultLocale;
        Map<String, Object> placeholders = buildPlaceholders(uhc, extra);
        List<String> out = new ArrayList<>();
        for (String line : getRawLines(key, locale)) out.add(applyPlaceholders(line, placeholders));
        return out;
    }

    public List<String> getLines(Lang key, Player player) {
        return getLines(key, player, null);
    }

    public List<String> getLines(Lang key) {
        return getLines(key, null, null);
    }

    public void send(Lang key, Player player) {
        if (player != null && player.isOnline()) {
            player.sendMessage(get(key, player));
        }
    }

    public void send(Lang key, Player player, Map<String, Object> extra) {
        if (player != null && player.isOnline()) {
            player.sendMessage(get(key, player, extra));
        }
    }

    public void sendAll(Lang key) {
        sendAll(key, null);
    }

    public void sendAll(Lang key, Map<String, Object> extra) {
        if (UHCPlayerManager.get() == null) return;
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            Player p = uhcPlayer.getPlayer();
            if (p != null && p.isOnline()) {
                p.sendMessage(get(key, p, extra));
            }
        }
    }

    public Map<String, Object> buildPlaceholders(UHCPlayer uhcPlayer, Map<String, Object> extra) {
        Map<String, Object> ph = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        ph.put("%time%", TIME_FORMAT.format(now));
        ph.put("%date%", DATE_FORMAT.format(now));

        if (Bukkit.isPrimaryThread()) {
            try {
                if (Common.get() != null && Common.get().getArena() != null) {
                    ph.put("%border%", String.valueOf((int) Common.get().getArena().getWorldBorder().getSize()));
                }
            } catch (Exception error) { DebugLog.warnOnce("Lang", "echec silencieux", error); }
        }
        ph.putIfAbsent("%border%", "N/A");

        try {
            if (UHCManager.get() != null) {
                ph.put("%timer%", UHCManager.get().getTimerFormatted());
                ph.put("%gamestate%", UHCManager.get().getGameState().name());
                ph.put("%slot%", String.valueOf(UHCManager.get().getSlot()));
                ph.put("%diamond_limite%", String.valueOf(UHCManager.get().getDiamondLimit()));
            }
        } catch (Exception error) { DebugLog.warnOnce("Lang", "echec silencieux", error); }
        ph.putIfAbsent("%timer%", "00:00");
        ph.putIfAbsent("%gamestate%", "UNKNOWN");
        ph.putIfAbsent("%slot%", "0");
        ph.putIfAbsent("%diamond_limite%", "0");

        try {
            if (Common.get() != null) {
                ph.put("%main_color%", Common.get().getMainColor() != null ? Common.get().getMainColor() : "§e§l");
                ph.put("%infotag%", Common.get().getInfoTag() != null ? Common.get().getInfoTag() : "");
                ph.put("%servertag%", Common.get().getServertag() != null ? Common.get().getServertag() : "");
                ph.put("%servername%", Common.get().getServername() != null ? Common.get().getServername() : "NovaUHC");
                ph.put("%serveurname%", ph.get("%servername%"));
            }
        } catch (Exception error) { DebugLog.warnOnce("Lang", "echec silencieux", error); }
        ph.putIfAbsent("%main_color%", "§e§l");
        ph.putIfAbsent("%infotag%", "");
        ph.putIfAbsent("%servertag%", "");
        ph.putIfAbsent("%servername%", "NovaUHC");
        ph.putIfAbsent("%serveurname%", "NovaUHC");

        try {
            if (UHCPlayerManager.get() != null) {
                ph.put("%players%", String.valueOf(UHCUtils.ServerMonitor.displayedPlayerCount(
                        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size())));
            }
        } catch (Exception error) { DebugLog.warnOnce("Lang", "echec silencieux", error); }
        ph.putIfAbsent("%players%", "0");

        try {
            ph.put("%host%", PlayerConnectionEvent.getHost() != null
                    ? PlayerConnectionEvent.getHost().getName() : "N/A");
        } catch (Exception error) {
            DebugLog.warnOnce("Lang", "echec silencieux", error);
            ph.put("%host%", "N/A");
        }

        if (uhcPlayer != null && uhcPlayer.getPlayer() != null) {
            ph.put("%player%", uhcPlayer.getPlayer().getName());
            ph.put("%kills%", String.valueOf(uhcPlayer.getKill()));
            if (Bukkit.isPrimaryThread()) {
                ph.put("%health%", String.format("%.1f", uhcPlayer.getPlayer().getHealth()));
            }
            ph.putIfAbsent("%health%", "0");
            ph.put("%mined_diamond%", String.valueOf(uhcPlayer.getMinedDiamond()));
            ph.put("%team%", uhcPlayer.getTeam().isPresent() ? uhcPlayer.getTeam().get().name() : "Solo");
            ph.put("%state%", uhcPlayer.isPlaying() ? "En jeu" : "Mort");
            ph.put("%killer%", uhcPlayer.getKiller() != null ? uhcPlayer.getKiller().getName() : "N/A");

            try {
                if (ScenarioManager.get() != null) {
                    String role = ScenarioManager.get().getActiveSpecialScenarios().stream()
                            .filter(s -> s instanceof ScenarioRole)
                            .findFirst()
                            .map(s -> ((ScenarioRole<?>) s).getRoleByUHCPlayer(uhcPlayer).getName())
                            .orElse("N/A");
                    ph.put("%roles%", role);
                }
            } catch (Exception error) { DebugLog.warnOnce("Lang", "echec silencieux", error); }
            ph.putIfAbsent("%roles%", "N/A");

            if (Bukkit.isPrimaryThread()) {
                try {
                    Location position = uhcPlayer.getPlayer().getLocation();
                    if (Common.get() != null && Common.get().getArena() != null
                            && Common.get().getArena().equals(position.getWorld())) {
                        Location center = Common.get().getArena().getHighestBlockAt(0, 0).getLocation();
                        String arrow = GeoUtils.getArrowDirection(
                                uhcPlayer.getPlayer().getLocation(),
                                center,
                                uhcPlayer.getPlayer().getLocation().getYaw()
                        );
                        ph.put("%arrow%", arrow);
                        ph.put("%distance%", String.valueOf(uhcPlayer.getDistanceToCenter(center)));
                    }
                } catch (Exception error) { DebugLog.warnOnce("Lang", "echec silencieux", error); }
            }
            ph.putIfAbsent("%arrow%", "→");
            ph.putIfAbsent("%distance%", "N/A");
        } else {
            ph.putIfAbsent("%player%", "Unknown");
            ph.putIfAbsent("%kills%", "0");
            ph.putIfAbsent("%health%", "0");
            ph.putIfAbsent("%mined_diamond%", "0");
            ph.putIfAbsent("%team%", "Solo");
            ph.putIfAbsent("%state%", "Unknown");
            ph.putIfAbsent("%killer%", "N/A");
            ph.putIfAbsent("%roles%", "N/A");
            ph.putIfAbsent("%arrow%", "→");
            ph.putIfAbsent("%distance%", "N/A");
        }

        if (extra != null) {
            extra.forEach((k, v) -> ph.put(k, v));
        }

        return ph;
    }

    public Map<String, Object> buildPlaceholders(Map<String, Object> extra) {
        return buildPlaceholders(null, extra);
    }

    public String getServerDefaultLocale() {
        return serverDefaultLocale;
    }

    public Set<String> getAvailableLocales() {
        return Collections.unmodifiableSet(translations.keySet());
    }

    private static String applyPlaceholders(String msg, Map<String, Object> placeholders) {
        for (Map.Entry<String, Object> e : placeholders.entrySet()) {
            if (e.getValue() != null) {
                msg = msg.replace(e.getKey(), e.getValue().toString());
            }
        }
        return msg;
    }
}
