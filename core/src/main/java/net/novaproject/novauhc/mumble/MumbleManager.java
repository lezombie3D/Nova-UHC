package net.novaproject.novauhc.mumble;

import net.novaproject.novauhc.debug.DebugLog;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.api.ApiManager;
import net.novaproject.novauhc.event.UhcGameEvents.UhcScatterEndEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerDeathEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerReconnectEvent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.MumbleUiLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MumbleManager implements IMumbleManager {

    private static MumbleManager instance;

    public static MumbleManager get() {
        if (instance == null) instance = new MumbleManager();
        return instance;
    }

    private volatile String sessionUuid;

    /**
     * Dernier état vocal connu, par pseudo Minecraft en minuscules. Absent = pas connecté.
     *
     * Sert DEUX usages, et c'est ce qui rend l'interface instantanée : la pastille du tab, et
     * l'ouverture du menu de modération, qui se dessine désormais depuis ce cache au lieu
     * d'attendre un aller-retour HTTP + Ice à chaque fois.
     */
    private final Map<String, LiveUser> liveCache = new ConcurrentHashMap<>();

    /** true si ce joueur est connecté au vocal ET que son mod d'audio positionnel est actif. */
    public boolean isLinked(String playerName) {
        LiveUser u = playerName == null ? null : liveCache.get(playerName.toLowerCase());
        return u != null && u.linked();
    }

    /** true si ce joueur est connecté au vocal, lié ou non. */
    public boolean isConnected(String playerName) {
        return playerName != null && liveCache.containsKey(playerName.toLowerCase());
    }

    /** Vue immédiate des connectés au vocal (peut dater de quelques secondes). */
    public List<LiveUser> getCachedLiveUsers() {
        return new ArrayList<>(liveCache.values());
    }

    /**
     * Sonde périodiquement l'état vocal pour alimenter l'affichage (pastille du tab).
     *
     * Un sondage plutôt qu'un push : l'API ne rappelle jamais le serveur Minecraft (le vocal est
     * pull-only), et un état vocal en retard de quelques secondes n'a aucune conséquence — il
     * n'autorise ni ne refuse rien, contrairement à la politique mute/deaf qui, elle, est
     * appliquée côté service.
     */
    public void startStatePolling() {
        long ticks = 20L * Math.max(1, Main.get().getConfig().getInt("mumble.state-poll-seconds", 3));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEnabled() || sessionUuid == null) {
                    liveCache.clear();
                    return;
                }
                refreshCache();
            }
        }.runTaskTimerAsynchronously(Main.get(), ticks, ticks);
    }

    /** Relit l'état vocal et met le cache à jour. Le futur permet de chaîner un rendu. */
    public CompletableFuture<Void> refreshCache() {
        return fetchLiveUsers().thenAccept(users -> {
            Map<String, LiveUser> next = new HashMap<>();
            for (LiveUser u : users) next.put(u.name().toLowerCase(), u);
            liveCache.keySet().retainAll(next.keySet());
            liveCache.putAll(next);
            placed.retainAll(next.keySet());
            // Pas de notification : TabVoiceStatus relit ce cache à son propre rythme,
            // ce qui lui permet de couvrir aussi les changements d'équipe/rang.
            Bukkit.getScheduler().runTask(Main.get(), this::placeLateArrivals);
        }).exceptionally(ex -> null);
    }

    /** Joueurs déjà placés à leur arrivée : on ne les replace jamais deux fois. */
    private final Set<String> placed = ConcurrentHashMap.newKeySet();

    /**
     * Amène dans le bon salon un joueur qui rejoint le vocal APRÈS le lancement de la partie.
     *
     * Le lien de connexion dépose toujours au Lobby (c'est le seul salon où `Enter` est
     * autorisé, ce qui empêche un mort de s'inviter en Partie) : un retardataire y restait donc
     * bloqué, puisque la bascule collective avait déjà eu lieu et que plus rien ne repassait.
     *
     * Chaque joueur n'est placé QU'UNE FOIS, à sa première apparition : sans ça, on annulerait
     * en boucle le bouton host « Déplacer au lobby ». Nécessairement côté plugin — le service
     * ignore la phase de jeu et qui est encore vivant.
     */
    private void placeLateArrivals() {
        if (!UHCManager.get().isGame()) return;
        for (LiveUser u : getCachedLiveUsers()) {
            if (!"lobby".equals(u.channel())) continue;
            if (!placed.add(u.name().toLowerCase())) continue;
            Player p = Bukkit.getPlayerExact(u.name());
            if (p == null) continue; // connecté au vocal mais pas en jeu : on le laisse
            UHCPlayer uhc = UHCPlayerManager.get().getPlayer(p);
            boolean alive = uhc != null && uhc.isPlaying();
            moderate("move", u.name(), alive ? "game" : "spectators", null).exceptionally(ex -> null);
        }
    }

    /** Corrige le cache sans attendre le prochain relevé, après une action de modération. */
    public void patchCache(List<LiveUser> users) {
        for (LiveUser u : users) liveCache.put(u.name().toLowerCase(), u);
    }

    public boolean isEnabled() {
        String key = Main.get().getConfig().getString("mumble.access-key", "");
        return Main.get().getConfig().getBoolean("mumble.enabled", false) && key != null && !key.isEmpty();
    }

    public boolean hasSession() { return sessionUuid != null; }

    public String getSessionUuid() { return sessionUuid; }

    /**
     * Provisionne la session vocale. IDEMPOTENT côté API : rappeler cette méthode sur une
     * session déjà ouverte ne la recrée pas, elle met seulement à jour ses `options` — c'est
     * ce qui permet au host de basculer l'anonymisation des pseudos depuis le menu.
     *
     * `teams` est ignoré par l'API (les salons d'équipe ont été supprimés) ; le paramètre reste
     * dans la signature parce qu'il fait partie du contrat IMumbleManager du module `api`.
     */
    public CompletableFuture<Void> provision(int teams, String title, Player host) {
        if (!isEnabled()) return CompletableFuture.completedFuture(null);
        JsonObject body = new JsonObject();
        String gameUuid = ApiManager.get().getCurrentGameUuid();
        if (gameUuid != null) body.addProperty("gameUuid", gameUuid);
        if (title != null && !title.isEmpty()) body.addProperty("title", title);
        JsonObject options = new JsonObject();
        options.addProperty("scrambleNames", UHCManager.get().isMumbleScrambleNames());
        body.add("options", options);
        if (host != null) {
            JsonObject hp = new JsonObject();
            hp.addProperty("uuid", host.getUniqueId().toString());
            hp.addProperty("name", host.getName());
            body.add("hostPlayer", hp);
        }
        return ApiManager.get().callMumble("POST", "/mumble/sessions", body)
                .thenAccept(resp -> {
                    JsonObject data = resp.getAsJsonObject("data");
                    this.sessionUuid = data.get("sessionUuid").getAsString();
                    ApiManager.get().log.info("[Mumble] Session vocale provisionnée : " + sessionUuid);
                })
                .exceptionally(ex -> {
                    ApiManager.get().log.warning("[Mumble] Provision échouée : " + rootMessage(ex));
                    return null;
                });
    }

    public void close() {
        String s = sessionUuid;
        if (s == null) return;
        sessionUuid = null;
        if (!isEnabled()) return;
        ApiManager.get().callMumble("POST", "/mumble/sessions/" + s + "/close", new JsonObject())
                .exceptionally(ex -> {
                    ApiManager.get().log.warning("[Mumble] Fermeture échouée : " + rootMessage(ex));
                    return null;
                });
    }

    /**
     * Bascule « salle d'attente → partie » : tout le monde passe du Lobby au salon Partie.
     *
     * On n'exclut PERSONNE, même un joueur dont le mod d'audio positionnel semble inactif.
     * Ce déplacement tombe pendant le téléport du scatter, où tous les clients gèlent le temps
     * de charger le terrain : Mumble lâche le link au bout de 5 s d'inactivité, donc un
     * instantané pris ici déclare non liés des joueurs parfaitement équipés — et les laissait
     * bloqués au Lobby sans que rien ne les reprenne. La coupure micro/son des vrais non-liés
     * est appliquée côté service, sur deux relevés espacés, et se rétablit toute seule.
     */
    public void startGame() {
        if (!isEnabled() || sessionUuid == null) return;
        JsonObject body = new JsonObject();
        body.addProperty("action", "moveAll");
        body.addProperty("channel", "game");
        ApiManager.get().callMumble("POST", sessionPath("/moderate"), body)
                .exceptionally(ex -> {
                    ApiManager.get().log.warning("[Mumble] Bascule en partie échouée : " + rootMessage(ex));
                    return null;
                });
    }

    /** Replace un joueur dans le salon de la partie (retour après une reconnexion Minecraft). */
    public void moveToGame(String playerName) {
        if (!isEnabled() || sessionUuid == null || playerName == null) return;
        moderate("move", playerName, "game", null).exceptionally(ex -> null);
    }

    /**
     * Ré-applique les options au serveur vocal après un changement dans le menu host, puis
     * redistribue les liens : le pseudo affiché dans Mumble est figé à la connexion, donc
     * activer/désactiver l'anonymisation n'a d'effet qu'après reconnexion du joueur.
     */
    public void applyOptions() {
        if (!isEnabled() || sessionUuid == null) return;
        provision(0, Main.get().getConfig().getString("api.server-name", "UHC Voice"), null)
                .thenRun(() -> Bukkit.getScheduler().runTask(Main.get(), this::broadcastJoinLinks));
    }

    public void playerDeath(String playerName) {
        if (!isEnabled() || sessionUuid == null || playerName == null) return;
        JsonObject body = new JsonObject();
        body.addProperty("event", "death");
        body.addProperty("playerName", playerName);
        ApiManager.get().callMumble("POST", sessionPath("/player-event"), body)
                .exceptionally(ex -> null);
    }

    public void broadcastJoinLinks() {
        if (!isEnabled() || sessionUuid == null) return;
        for (Player p : Bukkit.getOnlinePlayers()) sendJoinLink(p);
    }

    public void sendJoinLink(Player player) {
        if (!isEnabled()) { player.sendMessage(t(MumbleUiLang.DISABLED, player)); return; }
        if (sessionUuid == null) { player.sendMessage(t(MumbleUiLang.NO_SESSION, player)); return; }

        JsonObject body = new JsonObject();
        body.addProperty("playerName", player.getName());
        ApiManager.get().callMumble("POST", sessionPath("/join-link"), body)
                .thenAccept(resp -> {
                    JsonObject data = resp.getAsJsonObject("data");
                    String url = data.get("url").getAsString();
                    // Pseudo vocal anonymisé : le joueur doit pouvoir se reconnaître dans Mumble.
                    String nick = data.has("mumbleName") && !data.get("mumbleName").isJsonNull()
                            ? data.get("mumbleName").getAsString() : null;
                    String shown = nick != null && !nick.equals(player.getName()) ? nick : null;
                    Bukkit.getScheduler().runTask(Main.get(), () -> sendConnectMessage(player, url, shown));
                })
                .exceptionally(ex -> {
                    Bukkit.getScheduler().runTask(Main.get(), () -> player.sendMessage(t(MumbleUiLang.ERROR, player)));
                    return null;
                });
    }

    private void sendConnectMessage(Player player, String mumbleUrl, String voiceNick) {
        String clickUrl = wrapWithLandingPage(mumbleUrl);

        player.sendMessage(t(MumbleUiLang.CONNECT_HEADER, player));
        player.sendMessage(t(MumbleUiLang.CONNECT_DESC, player));
        if (voiceNick != null) {
            player.sendMessage(LangManager.get().get(MumbleUiLang.CONNECT_NICK, player, Map.of("%nick%", voiceNick)));
        }

        TextComponent button = new TextComponent(t(MumbleUiLang.CONNECT_BUTTON, player));
        button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, clickUrl));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(t(MumbleUiLang.CONNECT_HOVER, player)).create()));
        player.spigot().sendMessage(button);

        player.sendMessage(t(MumbleUiLang.CONNECT_FOOTER, player));
    }

    private String wrapWithLandingPage(String mumbleUrl) {
        String base = Main.get().getConfig().getString("mumble.link-base", "");
        if (base == null || base.isEmpty()) return mumbleUrl;
        String sep = base.contains("?") ? "&" : "?";
        return base + sep + "link=" + URLEncoder.encode(mumbleUrl, StandardCharsets.UTF_8);
    }

    public CompletableFuture<JsonObject> moderate(String action, String playerName, String channel, List<String> exceptNames) {
        if (!isEnabled() || sessionUuid == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("no session"));
        }
        JsonObject body = new JsonObject();
        body.addProperty("action", action);
        if (playerName != null) body.addProperty("playerName", playerName);
        if (channel != null) body.addProperty("channel", channel);
        if (exceptNames != null && !exceptNames.isEmpty()) {
            JsonArray arr = new JsonArray();
            exceptNames.forEach(n -> arr.add(new JsonPrimitive(n)));
            body.add("exceptNames", arr);
        }
        return ApiManager.get().callMumble("POST", sessionPath("/moderate"), body);
    }

    public CompletableFuture<List<LiveUser>> fetchLiveUsers() {
        if (!isEnabled() || sessionUuid == null) return CompletableFuture.completedFuture(new ArrayList<>());
        return ApiManager.get().callMumble("GET", sessionPath(""), null)
                .thenApply(resp -> {
                    List<LiveUser> out = new ArrayList<>();
                    try {
                        JsonObject data = resp.getAsJsonObject("data");
                        if (data != null && data.has("liveUsers") && data.get("liveUsers").isJsonArray()) {
                            for (JsonElement el : data.getAsJsonArray("liveUsers")) {
                                JsonObject u = el.getAsJsonObject();
                                out.add(new LiveUser(
                                        u.get("name").getAsString(),
                                        u.has("mute") && !u.get("mute").isJsonNull() && u.get("mute").getAsBoolean(),
                                        u.has("deaf") && !u.get("deaf").isJsonNull() && u.get("deaf").getAsBoolean(),
                                        u.has("linked") && !u.get("linked").isJsonNull() && u.get("linked").getAsBoolean(),
                                        u.has("channelKey") && !u.get("channelKey").isJsonNull()
                                                ? u.get("channelKey").getAsString() : null));
                            }
                        }
                    } catch (Exception error) { DebugLog.warnOnce("Mumble", "echec silencieux", error); }
                    return out;
                });
    }

    private String t(MumbleUiLang key, Player player) {
        return LangManager.get().get(key, player);
    }

    private String sessionPath(String suffix) {
        return "/mumble/sessions/" + sessionUuid + suffix;
    }

    private static String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage();
    }

    /**
     * Vue légère d'un joueur connecté au vocal. `name` est toujours le pseudo MINECRAFT
     * (l'API le ré-résout même quand les pseudos affichés dans Mumble sont anonymisés).
     * `linked` = le mod d'audio positionnel tourne ET annonce bien ce joueur.
     * `channel` = clé logique du salon Mumble (lobby / game / spectators), null si inconnue.
     */
    public record LiveUser(String name, boolean mute, boolean deaf, boolean linked, String channel) {}

    public static class MumbleListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onJoin(PlayerJoinEvent e) {
            MumbleManager mm = MumbleManager.get();
            if (mm.isEnabled() && mm.hasSession()) {
                mm.sendJoinLink(e.getPlayer());
            }
        }

        /**
         * Bascule Lobby → Partie à la FIN du scatter, pas au démarrage : à ce moment-là les
         * joueurs ont eu le temps de cliquer leur lien et de lancer MumbleLink, donc
         * `onlyLinked` ne laisse derrière que ceux qui n'ont réellement pas le mod.
         */
        @EventHandler(priority = EventPriority.MONITOR)
        public void onScatterEnd(UhcScatterEndEvent e) {
            MumbleManager.get().startGame();
        }

        @EventHandler
        public void onDeath(UhcPlayerDeathEvent e) {
            if (e.getBukkitEvent() == null || e.getBukkitEvent().getEntity() == null) return;
            MumbleManager.get().playerDeath(e.getBukkitEvent().getEntity().getName());
        }

        /**
         * Un joueur qui revient ne peut pas rentrer seul dans le salon Partie (`Enter` y est
         * refusé, c'est ce qui empêche les spectateurs de s'y inviter) : on l'y remet.
         */
        @EventHandler(priority = EventPriority.MONITOR)
        public void onReconnect(UhcPlayerReconnectEvent e) {
            if (!UHCManager.get().isGame()) return;
            Player p = e.getBukkitPlayer();
            if (p != null) MumbleManager.get().moveToGame(p.getName());
        }
    }
}
