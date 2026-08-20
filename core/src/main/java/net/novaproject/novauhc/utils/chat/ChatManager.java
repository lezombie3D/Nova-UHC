package net.novaproject.novauhc.utils.chat;

import net.novaproject.novauhc.lobby.RankManager;
import java.util.HashMap;

import java.util.Locale;
import net.novaproject.novauhc.game.Systems.SilenceSystem;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.lang.LangManager;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatManager {

    private static final ChatManager INSTANCE = new ChatManager();

    public static final String SPEC_CHANNEL_ID = "core:spec";
    public static final String LOBBY_CHANNEL_ID = "core:lobby";

    private final Map<Camps, Long> openChannels = new ConcurrentHashMap<>();
    private final Map<String, ChatChannel> channels = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastLobbyChat = new ConcurrentHashMap<>();
    private final Set<String> globalPrefixes = ConcurrentHashMap.newKeySet();

    private ChatManager() {
        globalPrefixes.add("!");
    }

    public static ChatManager get() {
        return INSTANCE;
    }

    public void handle(AsyncPlayerChatEvent event) {
        if (event == null || event.getPlayer() == null) return;

        Player player = event.getPlayer();
        String message = event.getMessage();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) {
            event.setCancelled(true);
            return;
        }

        if (SilenceSystem.isSilenced(player.getUniqueId())) {
            event.setCancelled(true);
            LangManager.get().send(CoreLang.COMMON_SILENCED, player, Map.of(
                    "%time%", SilenceSystem.remainingSeconds(player.getUniqueId())));
            return;
        }

        if (UHCManager.get().getGameState() != UHCManager.GameState.INGAME) {
            applyLobbyChat(event, player);
            return;
        }

        if (!uhcPlayer.isPlaying()) {
            applySpectatorChat(event, player);
            return;
        }

        ChatRoute route = findPrefixedRoute(player, message);
        if (route != null) {
            route.apply(event);
            return;
        }

        String globalPrefix = findGlobalPrefix(message);
        if (globalPrefix != null) {
            if (isGlobalChatBlocked(event, player)) return;
            String body = message.substring(globalPrefix.length()).trim();
            if (body.isEmpty()) {
                event.setCancelled(true);
                return;
            }
            event.setMessage(body);
            event.setFormat(LangManager.get().get(CoreLang.COMMON_CHAT_GLOBAL_FORMAT, player));
            return;
        }

        List<Scenario> chatScenarios = ScenarioManager.get().getActiveScenarios().stream()
                .filter(Scenario::hasCustomTeamTchat)
                .collect(Collectors.toList());

        if (!chatScenarios.isEmpty()) {
            chatScenarios.forEach(s -> s.onChatSpeak(player, message, event));
            return;
        }

        Camps openCamp = findOpenChannelFor(uhcPlayer);
        if (openCamp != null && !message.startsWith("!")) {
            applyCampChat(event, player, openCamp);
            return;
        }

        if (!uhcPlayer.getTeam().isPresent()) {
            if (isGlobalChatBlocked(event, player)) return;
            event.setFormat(LangManager.get().get(CoreLang.COMMON_SOLO_CHAT_FORMAT, player));
            return;
        }

        applyTeamChat(event, player, uhcPlayer, uhcPlayer.getTeam().get());
    }

    public void registerGlobalPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) return;
        globalPrefixes.add(prefix);
    }

    public void unregisterGlobalPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) return;
        globalPrefixes.remove(prefix);
    }

    public Set<String> getGlobalPrefixes() {
        return Collections.unmodifiableSet(new HashSet<>(globalPrefixes));
    }

    public void registerChannel(ChatChannel channel) {
        if (channel == null || channel.getId() == null || channel.getId().isEmpty()) return;
        channels.put(channel.getId().toLowerCase(Locale.ROOT), channel);
    }

    public void unregisterChannel(String id) {
        if (id == null) return;
        channels.remove(id.toLowerCase(Locale.ROOT));
    }

    public ChatChannel getChannel(String id) {
        if (id == null) return null;
        ChatChannel channel = channels.get(id.toLowerCase(Locale.ROOT));
        if (channel != null && channel.isExpired()) {
            unregisterChannel(id);
            return null;
        }
        return channel;
    }

    public Set<String> getChannelIds() {
        cleanupExpiredChannels();
        return Collections.unmodifiableSet(channels.keySet());
    }

    public void clearChannels() {
        channels.clear();
    }

    public void resetLobbyCooldown(Player player) {
        if (player != null) {
            lastLobbyChat.remove(player.getUniqueId());
        }
    }

    public boolean send(String channelId, Player sender, String message) {
        ChatChannel channel = getChannel(channelId);
        if (channel == null || sender == null || message == null || message.trim().isEmpty()) return false;
        if (!channel.canWrite(sender)) return false;
        channel.markSent(sender);

        for (Player receiver : Bukkit.getOnlinePlayers()) {
            if (receiver == null || !receiver.isOnline()) continue;
            if (!channel.canRead(receiver)) continue;
            receiver.sendMessage(channel.format(sender, receiver, message));
        }
        return true;
    }

    public int sendSystem(String channelId, String message) {
        ChatChannel channel = getChannel(channelId);
        if (channel == null || message == null || message.trim().isEmpty()) return 0;

        int delivered = 0;
        for (Player receiver : Bukkit.getOnlinePlayers()) {
            if (receiver == null || !receiver.isOnline()) continue;
            if (!channel.canRead(receiver)) continue;
            receiver.sendMessage(message);
            delivered++;
        }
        return delivered;
    }

    public ChatChannel createGlobalChannel(String id, String prefix) {
        ChatChannel channel = new ChatChannel(id)
                .prefix(prefix)
                .readerRule((player, uhcPlayer) -> true)
                .writerRule((player, uhcPlayer) -> true)
                .formatter((sender, receiver, message, ch) ->
                        formatVanilla(LangManager.get().get(CoreLang.COMMON_CHAT_GLOBAL_FORMAT, sender), sender, message));
        registerChannel(channel);
        return channel;
    }

    public ChatChannel createTeamChannel(String id, UHCTeam team, String prefix) {
        ChatChannel channel = new ChatChannel(id)
                .prefix(prefix)
                .team(team)
                .readerRule((player, uhcPlayer) -> isInTeam(uhcPlayer, team))
                .writerRule((player, uhcPlayer) -> isInTeam(uhcPlayer, team))
                .formatter((sender, receiver, message, ch) ->
                        formatVanilla(LangManager.get().get(CoreLang.COMMON_TEAM_CHAT_FORMAT, sender,
                                Map.of("%team%", team == null ? "Team" : team.prefix())), sender, message));
        registerChannel(channel);
        return channel;
    }

    public ChatChannel getOrCreateTeamChannel(UHCTeam team) {
        if (team == null) return null;
        ChatChannel channel = getChannel(teamChannelId(team));
        if (channel != null) return channel;
        channel = createTeamChannel(teamChannelId(team), team, null).defaultTeamRoute(true);
        registerChannel(channel);
        return channel;
    }

    public ChatChannel createGroupChannel(String id, Collection<UUID> members, String prefix) {
        ChatChannel channel = new ChatChannel(id)
                .prefix(prefix)
                .formatter((sender, receiver, message, ch) ->
                        formatVanilla(LangManager.get().get(CoreLang.COMMON_BOND_CHAT_FORMAT, receiver), sender, message));
        if (members != null) {
            members.forEach(channel::addMember);
        }
        registerChannel(channel);
        return channel;
    }

    public ChatChannel createCampChannel(String id, Camps camp, String prefix) {
        ChatChannel channel = new ChatChannel(id)
                .prefix(prefix)
                .readerRule((player, uhcPlayer) -> isInCamp(player, camp))
                .writerRule((player, uhcPlayer) -> isInCamp(player, camp))
                .formatter((sender, receiver, message, ch) ->
                        formatVanilla(LangManager.get().get(CoreLang.COMMON_CAMP_CHAT_FORMAT, sender,
                                Map.of("%camp%", camp == null ? "Camp" : camp.getColor() + camp.getName())), sender, message));
        registerChannel(channel);
        return channel;
    }

    public ChatChannel addIndirectReader(String channelId, Player player) {
        ChatChannel channel = getChannel(channelId);
        if (channel != null) channel.addIndirectReader(player);
        return channel;
    }

    public ChatChannel addIndirectWriter(String channelId, Player player) {
        ChatChannel channel = getChannel(channelId);
        if (channel != null) channel.addIndirectWriter(player);
        return channel;
    }

    public ChatChannel addIndirectMember(String channelId, Player player) {
        ChatChannel channel = getChannel(channelId);
        if (channel != null) channel.addIndirectMember(player);
        return channel;
    }

    public ChatChannel addIndirectTeamReader(UHCTeam team, Player player) {
        ChatChannel channel = getOrCreateTeamChannel(team);
        if (channel != null) channel.addIndirectReader(player);
        return channel;
    }

    public ChatChannel addIndirectTeamWriter(UHCTeam team, Player player) {
        ChatChannel channel = getOrCreateTeamChannel(team);
        if (channel != null) channel.addIndirectWriter(player);
        return channel;
    }

    public ChatChannel addIndirectTeamMember(UHCTeam team, Player player) {
        ChatChannel channel = getOrCreateTeamChannel(team);
        if (channel != null) channel.addIndirectMember(player);
        return channel;
    }

    public ChatChannel addIndirectReaders(String channelId, Collection<Player> players) {
        ChatChannel channel = getChannel(channelId);
        if (channel == null || players == null) return channel;
        players.forEach(channel::addIndirectReader);
        return channel;
    }

    public ChatChannel addIndirectWriters(String channelId, Collection<Player> players) {
        ChatChannel channel = getChannel(channelId);
        if (channel == null || players == null) return channel;
        players.forEach(channel::addIndirectWriter);
        return channel;
    }

    public void openCampChannel(Camps camp, int durationSeconds) {
        if (camp == null || durationSeconds <= 0) return;
        openChannels.put(camp, System.currentTimeMillis() + durationSeconds * 1000L);

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Role role = KPIBuilder.roleOf(uhcPlayer);
            if (role == null || role.getCamp() == null || !role.getCamp().is(camp)) continue;
            Player player = uhcPlayer.getPlayer();
            if (player == null || !player.isOnline()) continue;
            LangManager.get().send(CoreLang.COMMON_CAMP_CHANNEL_OPENED, player,
                    Map.of("%camp%", camp.getColor() + camp.getName(), "%time%", durationSeconds));
        }
    }

    public void closeCampChannel(Camps camp) {
        if (camp == null) return;
        openChannels.keySet().removeIf(key -> key.is(camp));
    }

    public Camps findOpenChannelFor(UHCPlayer uhcPlayer) {
        if (uhcPlayer == null || openChannels.isEmpty()) return null;
        cleanupExpired();
        Role role = KPIBuilder.roleOf(uhcPlayer);
        if (role == null || role.getCamp() == null) return null;
        for (Camps key : openChannels.keySet()) {
            if (role.getCamp().is(key)) return key;
        }
        return null;
    }

    public boolean isInCamp(Player player, Camps camp) {
        if (player == null || camp == null) return false;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return false;
        Role role = KPIBuilder.roleOf(uhcPlayer);
        return role != null && role.getCamp() != null && role.getCamp().is(camp);
    }

    public boolean isInTeam(UHCPlayer uhcPlayer, UHCTeam team) {
        return uhcPlayer != null && team != null && uhcPlayer.getTeam().isPresent() && uhcPlayer.getTeam().get() == team;
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        openChannels.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    private void cleanupExpiredChannels() {
        channels.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().isExpired());
    }

    public ChatChannel lobbyChannel() {
        ChatChannel channel = channels.get(LOBBY_CHANNEL_ID);
        if (channel != null) return channel;
        channel = new ChatChannel(LOBBY_CHANNEL_ID)
                .readerRule((player, uhcPlayer) -> true)
                .writerRule((player, uhcPlayer) -> true)
                .eventFormatter((sender, message, ch) ->
                        LangManager.get().get(CoreLang.COMMON_LOBBY_CHAT_FORMAT, sender));
        registerChannel(channel);
        return channel;
    }

    public ChatChannel specChannel() {
        ChatChannel channel = channels.get(SPEC_CHANNEL_ID);
        if (channel != null) return channel;
        channel = new ChatChannel(SPEC_CHANNEL_ID)
                .readerRule((player, uhcPlayer) -> uhcPlayer != null && !uhcPlayer.isPlaying())
                .writerRule((player, uhcPlayer) -> uhcPlayer != null && !uhcPlayer.isPlaying())
                .eventFormatter((sender, message, ch) ->
                        LangManager.get().get(CoreLang.COMMON_SPEC_CHAT_FORMAT, sender));
        registerChannel(channel);
        return channel;
    }

    private boolean isGlobalChatBlocked(AsyncPlayerChatEvent event, Player player) {
        if (!UHCManager.get().isChatDisabled()) return false;
        LangManager.get().send(CoreLang.COMMON_CHAT_DISABLED, player);
        event.setCancelled(true);
        return true;
    }

    private void applyLobbyChat(AsyncPlayerChatEvent event, Player player) {
        int floodMs = UHCManager.get().getLobbyChatCooldownMs();
        if (floodMs > 0 && !RankManager.isStaff(player)) {
            Long last = lastLobbyChat.get(player.getUniqueId());
            long now = System.currentTimeMillis();
            if (last != null && now - last < floodMs) {
                event.setCancelled(true);
                return;
            }
            lastLobbyChat.put(player.getUniqueId(), now);
        }
        ChatChannel channel = lobbyChannel();
        String format = channel.formatEvent(player, event.getMessage());
        event.setFormat(format != null ? format : LangManager.get().get(CoreLang.COMMON_LOBBY_CHAT_FORMAT, player));
        event.getRecipients().removeIf(recipient -> !channel.canRead(recipient));
    }

    private void applySpectatorChat(AsyncPlayerChatEvent event, Player player) {
        ChatChannel channel = specChannel();
        String format = channel.formatEvent(player, event.getMessage());
        event.setFormat(format != null ? format : LangManager.get().get(CoreLang.COMMON_SPEC_CHAT_FORMAT, player));
        event.getRecipients().removeIf(recipient -> !channel.canRead(recipient));
    }

    private void applyCampChat(AsyncPlayerChatEvent event, Player player, Camps openCamp) {
        event.setFormat(LangManager.get().get(CoreLang.COMMON_CAMP_CHAT_FORMAT, player,
                Map.of("%camp%", openCamp.getColor() + openCamp.getName())));
        event.getRecipients().removeIf(recipient -> !isInCamp(recipient, openCamp));
    }

    private void applyTeamChat(AsyncPlayerChatEvent event, Player player, UHCPlayer uhcPlayer, UHCTeam team) {
        ChatChannel channel = findDefaultTeamRoute(player, uhcPlayer, team);
        if (channel != null) {
            if (!channel.canWrite(player)) {
                event.setCancelled(true);
                return;
            }
            event.getRecipients().removeIf(p -> !channel.canRead(p));
            String format = channel.formatEvent(player, event.getMessage());
            if (format == null) {
                format = LangManager.get().get(CoreLang.COMMON_TEAM_CHAT_FORMAT, player,
                        Map.of("%team%", channel.getTeam() == null ? team.prefix() : channel.getTeam().prefix()));
            }
            event.setFormat(format);
            return;
        }

        event.getRecipients().removeIf(p ->
                team.getPlayers().stream().noneMatch(u -> u.getPlayer() != null && u.getPlayer().equals(p))
        );
        event.setFormat(LangManager.get().get(CoreLang.COMMON_TEAM_CHAT_FORMAT, player,
                Map.of("%team%", team.prefix())));
    }

    private ChatRoute findPrefixedRoute(Player player, String message) {
        if (message == null) return null;
        cleanupExpiredChannels();
        boolean memberBlocked = false;
        for (ChatChannel channel : channels.values().stream()
                .filter(channel -> channel.getPrefix() != null && !channel.getPrefix().isEmpty())
                .sorted((left, right) -> Integer.compare(right.getPrefix().length(), left.getPrefix().length()))
                .collect(Collectors.toList())) {
            String prefix = channel.getPrefix();
            if (!message.startsWith(prefix)) continue;
            if (!channel.matchesPrefixBoundary(message)) continue;
            if (!channel.isWriterMember(player)) continue;
            String body = message.substring(prefix.length()).trim();
            if (body.isEmpty()) {
                return event -> event.setCancelled(true);
            }
            if (!channel.canWrite(player)) {
                memberBlocked = true;
                continue;
            }
            return event -> {
                event.setCancelled(true);
                channel.markSent(player);
                for (Player receiver : Bukkit.getOnlinePlayers()) {
                    if (receiver == null || !receiver.isOnline()) continue;
                    if (!channel.canRead(receiver)) continue;
                    receiver.sendMessage(channel.format(player, receiver, body));
                }
            };
        }
        if (memberBlocked) {
            return event -> event.setCancelled(true);
        }
        return null;
    }

    private String findGlobalPrefix(String message) {
        if (message == null || globalPrefixes.isEmpty()) return null;
        return globalPrefixes.stream()
                .filter(prefix -> prefix != null && !prefix.isEmpty() && message.startsWith(prefix))
                .sorted((left, right) -> Integer.compare(right.length(), left.length()))
                .findFirst()
                .orElse(null);
    }

    private String formatVanilla(String format, Player sender, String message) {
        return String.format(format, sender == null ? "" : sender.getDisplayName(), message);
    }

    private ChatChannel findDefaultTeamRoute(Player player, UHCPlayer uhcPlayer, UHCTeam fallbackTeam) {
        cleanupExpiredChannels();
        List<ChatChannel> defaultRoutes = channels.values().stream()
                .filter(channel -> channel != null && channel.isDefaultTeamRoute())
                .filter(channel -> channel.canWrite(player))
                .sorted((left, right) -> left.getId().compareToIgnoreCase(right.getId()))
                .collect(Collectors.toList());

        for (ChatChannel channel : defaultRoutes) {
            if (channel.isIndirectWriter(player)) return channel;
        }

        for (ChatChannel channel : defaultRoutes) {
            if (channel.getTeam() == fallbackTeam) return channel;
        }

        return null;
    }

    private String teamChannelId(UHCTeam team) {
        return "team:" + team.name();
    }

    private interface ChatRoute {
        void apply(AsyncPlayerChatEvent event);
    }

    public interface ChatAccessRule {
        boolean test(Player player, UHCPlayer uhcPlayer);
    }

    public interface ChatFormatter {
        String format(Player sender, Player receiver, String message, ChatChannel channel);
    }

    public interface ChatEventFormatter {
        String format(Player sender, String message, ChatChannel channel);
    }

    public static class ChatChannel {

        private final String id;
        private String prefix;
        private UHCTeam team;
        private boolean defaultTeamRoute;
        private boolean prefixRequiresWhitespaceBoundary;
        private long expiresAt = -1L;
        private int maxMessagesPerPlayer = -1;
        private final Set<UUID> readers = ConcurrentHashMap.newKeySet();
        private final Set<UUID> writers = ConcurrentHashMap.newKeySet();
        private final Set<UUID> indirectReaders = ConcurrentHashMap.newKeySet();
        private final Set<UUID> indirectWriters = ConcurrentHashMap.newKeySet();
        private final Map<UUID, Integer> sentMessages = new ConcurrentHashMap<>();
        private ChatAccessRule readerRule;
        private ChatAccessRule writerRule;
        private ChatEventFormatter eventFormatter;
        private ChatFormatter formatter = (sender, receiver, message, channel) ->
                "§8● §f" + sender.getName() + " §8• §7" + message;

        public ChatChannel(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public String getPrefix() {
            return prefix;
        }

        public UHCTeam getTeam() {
            return team;
        }

        public boolean isDefaultTeamRoute() {
            return defaultTeamRoute;
        }

        public ChatChannel prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public ChatChannel team(UHCTeam team) {
            this.team = team;
            return this;
        }

        public ChatChannel defaultTeamRoute(boolean defaultTeamRoute) {
            this.defaultTeamRoute = defaultTeamRoute;
            return this;
        }

        public ChatChannel prefixRequiresWhitespaceBoundary(boolean prefixRequiresWhitespaceBoundary) {
            this.prefixRequiresWhitespaceBoundary = prefixRequiresWhitespaceBoundary;
            return this;
        }

        public ChatChannel expiresInSeconds(int seconds) {
            if (seconds <= 0) {
                this.expiresAt = -1L;
            } else {
                this.expiresAt = System.currentTimeMillis() + seconds * 1000L;
            }
            sentMessages.clear();
            return this;
        }

        public ChatChannel maxMessagesPerPlayer(int maxMessagesPerPlayer) {
            this.maxMessagesPerPlayer = maxMessagesPerPlayer;
            if (maxMessagesPerPlayer < 0) {
                sentMessages.clear();
            }
            return this;
        }

        public ChatChannel readerRule(ChatAccessRule readerRule) {
            this.readerRule = readerRule;
            return this;
        }

        public ChatChannel writerRule(ChatAccessRule writerRule) {
            this.writerRule = writerRule;
            return this;
        }

        public ChatChannel formatter(ChatFormatter formatter) {
            if (formatter != null) this.formatter = formatter;
            return this;
        }

        public ChatChannel eventFormatter(ChatEventFormatter eventFormatter) {
            this.eventFormatter = eventFormatter;
            return this;
        }

        public ChatChannel addReader(Player player) {
            add(readers, player == null ? null : player.getUniqueId());
            return this;
        }

        public ChatChannel addWriter(Player player) {
            add(writers, player == null ? null : player.getUniqueId());
            return this;
        }

        public ChatChannel addIndirectReader(Player player) {
            add(indirectReaders, player == null ? null : player.getUniqueId());
            return this;
        }

        public ChatChannel addIndirectWriter(Player player) {
            add(indirectWriters, player == null ? null : player.getUniqueId());
            return this;
        }

        public ChatChannel addMember(Player player) {
            return addReader(player).addWriter(player);
        }

        public ChatChannel addIndirectMember(Player player) {
            return addIndirectReader(player).addIndirectWriter(player);
        }

        public ChatChannel addReader(UUID uuid) {
            add(readers, uuid);
            return this;
        }

        public ChatChannel addWriter(UUID uuid) {
            add(writers, uuid);
            return this;
        }

        public ChatChannel addIndirectReader(UUID uuid) {
            add(indirectReaders, uuid);
            return this;
        }

        public ChatChannel addIndirectWriter(UUID uuid) {
            add(indirectWriters, uuid);
            return this;
        }

        public ChatChannel addMember(UUID uuid) {
            return addReader(uuid).addWriter(uuid);
        }

        public ChatChannel addIndirectMember(UUID uuid) {
            return addIndirectReader(uuid).addIndirectWriter(uuid);
        }

        public ChatChannel remove(Player player) {
            return remove(player == null ? null : player.getUniqueId());
        }

        public ChatChannel remove(UUID uuid) {
            if (uuid == null) return this;
            readers.remove(uuid);
            writers.remove(uuid);
            indirectReaders.remove(uuid);
            indirectWriters.remove(uuid);
            sentMessages.remove(uuid);
            return this;
        }

        public Set<UUID> getReaders() {
            Set<UUID> result = new HashSet<>(readers);
            result.addAll(indirectReaders);
            return Collections.unmodifiableSet(result);
        }

        public Set<UUID> getWriters() {
            Set<UUID> result = new HashSet<>(writers);
            result.addAll(indirectWriters);
            return Collections.unmodifiableSet(result);
        }

        public boolean canRead(Player player) {
            if (player == null || isExpired()) return false;
            UUID uuid = player.getUniqueId();
            if (readers.contains(uuid) || indirectReaders.contains(uuid)) return true;
            return readerRule != null && readerRule.test(player, UHCPlayerManager.get().getPlayer(player));
        }

        public boolean canWrite(Player player) {
            if (!isWriterMember(player)) return false;
            return maxMessagesPerPlayer < 0
                    || sentMessages.getOrDefault(player.getUniqueId(), 0) < maxMessagesPerPlayer;
        }

        public boolean isWriterMember(Player player) {
            if (player == null || isExpired()) return false;
            UUID uuid = player.getUniqueId();
            if (writers.contains(uuid) || indirectWriters.contains(uuid)) return true;
            return writerRule != null && writerRule.test(player, UHCPlayerManager.get().getPlayer(player));
        }

        public boolean isIndirectReader(Player player) {
            return player != null && indirectReaders.contains(player.getUniqueId());
        }

        public boolean isIndirectWriter(Player player) {
            return player != null && indirectWriters.contains(player.getUniqueId());
        }

        private boolean matchesPrefixBoundary(String message) {
            if (!prefixRequiresWhitespaceBoundary || prefix == null || message == null) return true;
            return message.length() == prefix.length() || Character.isWhitespace(message.charAt(prefix.length()));
        }

        public void markSent(Player player) {
            if (player == null || maxMessagesPerPlayer < 0) return;
            sentMessages.merge(player.getUniqueId(), 1, Integer::sum);
        }

        public String format(Player sender, Player receiver, String message) {
            return formatter.format(sender, receiver, message, this);
        }

        public String formatEvent(Player sender, String message) {
            if (eventFormatter == null) return null;
            return eventFormatter.format(sender, message, this);
        }

        public boolean isExpired() {
            return expiresAt > 0 && expiresAt <= System.currentTimeMillis();
        }

        public void resetUsage() {
            sentMessages.clear();
        }

        private void add(Set<UUID> target, UUID uuid) {
            if (uuid != null) target.add(uuid);
        }
    }

    public static class MessageManager {
        private static final HashMap<UUID, UUID> lastMessage = new HashMap<>();

        public static void setLastMessage(UUID sender, UUID receiver) {
            lastMessage.put(sender, receiver);
            lastMessage.put(receiver, sender);
        }

        public static UUID getLastMessage(UUID player) {
            return lastMessage.get(player);
        }
    }
}
