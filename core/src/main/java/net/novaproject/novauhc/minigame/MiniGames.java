package net.novaproject.novauhc.minigame;

import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import java.util.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class MiniGames {

    private MiniGames() {
    }

    public static ItemStack pane(short durability, String name) {
        return new ItemCreator(Material.STAINED_GLASS_PANE)
                .setDurability(durability).setName(name).getItemstack();
    }

    public static ItemStack filler() {
        return pane((short) 15, " ");
    }

    public interface MiniGame {

        void start();

        void stop();

        void onPlayerQuit(UUID uuid);

        Collection<UUID> participants();
    }

    public enum MiniGameType {

        PUISSANCE4("puissance4", CoreLang.COMMON_MINIGAME_NAME_PUISSANCE4, true,  Puissance4Game::new, null),
        MORPION   ("morpion",    CoreLang.COMMON_MINIGAME_NAME_MORPION,    true,  MorpionGame::new,    null),
        BATTLESHIP("battleship", CoreLang.COMMON_MINIGAME_NAME_BATTLESHIP, true,  BattleshipGame::new,           null),
        MEMORY    ("memory",     CoreLang.COMMON_MINIGAME_NAME_MEMORY,     true,  MemoryGame::new,               null),
        SHIFUMI   ("shifumi",    CoreLang.COMMON_MINIGAME_NAME_SHIFUMI,    true,  ShifumiGame::new,              null),
        SNAKE     ("snake",      CoreLang.COMMON_MINIGAME_NAME_SNAKE,      false, null,                          SnakeGame::new);

        private final String key;
        private final CoreLang nameKey;
        private final boolean twoPlayer;
        private final BiFunction<Player, Player, MiniGame> duoFactory;
        private final Function<Player, MiniGame> soloFactory;

        MiniGameType(String key, CoreLang nameKey, boolean twoPlayer,
                     BiFunction<Player, Player, MiniGame> duoFactory,
                     Function<Player, MiniGame> soloFactory) {
            this.key = key;
            this.nameKey = nameKey;
            this.twoPlayer = twoPlayer;
            this.duoFactory = duoFactory;
            this.soloFactory = soloFactory;
        }

        public String getKey() {
            return key;
        }

        public CoreLang getNameKey() {
            return nameKey;
        }

        public String getDisplayName() {
            return LangManager.get().get(nameKey);
        }

        public String getDisplayName(Player viewer) {
            return LangManager.get().get(nameKey, viewer);
        }

        public String getTitle() {
            return LangManager.get().get(CoreLang.COMMON_MINIGAME_TITLE,
                    Map.of("%game%", getDisplayName()));
        }

        public boolean isTwoPlayer() {
            return twoPlayer;
        }

        public MiniGame create(Player p1, Player p2) {
            if (duoFactory == null) throw new IllegalStateException(name() + " n'est pas un jeu à deux joueurs");
            return duoFactory.apply(p1, p2);
        }

        public MiniGame create(Player player) {
            if (soloFactory == null) throw new IllegalStateException(name() + " n'est pas un jeu solo");
            return soloFactory.apply(player);
        }

        public static MiniGameType fromKey(String key) {
            for (MiniGameType type : values()) {
                if (type.key.equalsIgnoreCase(key)) return type;
            }
            return null;
        }

        public static List<String> duelKeys() {
            List<String> keys = new ArrayList<>();
            for (MiniGameType type : values()) {
                if (type.twoPlayer) keys.add(type.key);
            }
            return keys;
        }

        public static List<String> soloKeys() {
            List<String> keys = new ArrayList<>();
            for (MiniGameType type : values()) {
                if (!type.twoPlayer) keys.add(type.key);
            }
            return keys;
        }
    }

    public static final class MiniGameRegistry {

        private static final Map<UUID, MiniGame> ACTIVE = new HashMap<>();


        public static void register(MiniGame game) {
            for (UUID uuid : game.participants()) {
                ACTIVE.put(uuid, game);
            }
        }

        public static void unregister(MiniGame game) {
            ACTIVE.values().removeIf(g -> g == game);
        }

        public static boolean isPlaying(UUID uuid) {
            return ACTIVE.containsKey(uuid);
        }

        public static void stopAll() {
            Set<MiniGame> games = new HashSet<>(ACTIVE.values());
            ACTIVE.clear();
            games.forEach(MiniGame::stop);
        }

        public static void onQuit(UUID uuid) {
            MiniGame game = ACTIVE.get(uuid);
            if (game != null) {
                game.onPlayerQuit(uuid);
            }
        }
    }

    public static class BattleshipGame extends TurnBasedGridGame {

        private static final int SHIP_COUNT = 3;

        private final boolean[][] p1Ships;
        private final boolean[][] p2Ships;
        private final int[][] p1Shots;
        private final int[][] p2Shots;
        private int p1Placed = 0;
        private int p2Placed = 0;

        private enum Phase { PLACE, BATTLE }
        private Phase phase = Phase.PLACE;

        public BattleshipGame(Player p1, Player p2) {
            super(p1, p2, 7, 4);
            p1Ships = new boolean[getCols()][getRows()];
            p2Ships = new boolean[getCols()][getRows()];
            p1Shots = new int[getCols()][getRows()];
            p2Shots = new int[getCols()][getRows()];
        }

        @Override
        public String getTitle() {
            if (phase == Phase.PLACE) {
                return LangManager.get().get(CoreLang.COMMON_MINIGAME_TITLE_BATTLESHIP_PLACING, Map.of(
                        "%placed%", p1Placed + p2Placed,
                        "%total%", SHIP_COUNT * 2));
            }
            return MiniGames.MiniGameType.BATTLESHIP.getTitle();
        }

        @Override
        public boolean play(UUID uuid, int col, int row) {
            if (getState() != State.ONGOING) return false;
            if (!uuid.equals(currentPlayer()))  return false;
            if (col < 0 || col >= getCols() || row < 0 || row >= getRows()) return false;

            int p = playerNumber(uuid);

            if (phase == Phase.PLACE) {
                boolean[][] mine = (p == 1) ? p1Ships : p2Ships;
                int placed      = (p == 1) ? p1Placed : p2Placed;
                if (placed >= SHIP_COUNT) return false;
                if (mine[col][row])       return false;

                mine[col][row] = true;
                if (p == 1) p1Placed++; else p2Placed++;

                nextTurn();

                if (p1Placed >= SHIP_COUNT && p2Placed >= SHIP_COUNT) {
                    phase = Phase.BATTLE;

                }

                refreshViews();
                return true;
            }

            int[][] shots = (p == 1) ? p1Shots : p2Shots;
            if (shots[col][row] != 0) return false;

            boolean[][] enemy = (p == 1) ? p2Ships : p1Ships;
            boolean hit = enemy[col][row];

            shots[col][row] = hit ? 1 : -1;

            if (hit && hasAllShipsSunk(p)) {
                forceWin(p);
                return true;
            }

            if (!hit) nextTurn();

            refreshViews();
            return true;
        }

        private boolean hasAllShipsSunk(int attacker) {
            int[][] shots = (attacker == 1) ? p1Shots : p2Shots;
            boolean[][] enemy = (attacker == 1) ? p2Ships : p1Ships;
            for (int x = 0; x < getCols(); x++)
                for (int y = 0; y < getRows(); y++)
                    if (enemy[x][y] && shots[x][y] != 1) return false;
            return true;
        }

        @Override
        public ItemStack cellItemFor(int value, int col, int row, Player viewer) {
            int p = playerNumber(viewer.getUniqueId());
            if (p == 0) return cellItem(value, col, row);

            if (phase == Phase.PLACE) {
                boolean[][] mine = (p == 1) ? p1Ships : p2Ships;
                if (mine[col][row]) {
                    return MiniGames.pane((short) 5, t(CoreLang.COMMON_MINIGAME_BATTLESHIP_SHIP, viewer));
                }
                return MiniGames.pane((short) 3, " ");
            }

            int shot = ((p == 1) ? p1Shots : p2Shots)[col][row];
            if (shot == 0) {
                return MiniGames.pane((short) 3, "§9~");
            }
            if (shot == -1) {
                return MiniGames.pane((short) 7, t(CoreLang.COMMON_MINIGAME_BATTLESHIP_MISS, viewer));
            }
            return MiniGames.pane((short) 14, t(CoreLang.COMMON_MINIGAME_BATTLESHIP_HIT, viewer));
        }

        private static String t(CoreLang key, Player viewer) {
            return LangManager.get().get(key, viewer);
        }
    }

    public static class MemoryGame extends TurnBasedGridGame {

        private static final short[] CARD_COLORS = {11, 14, 9, 5, 4, 6, 2, 1};

        private final int[][] values;
        private final int[][] state;

        private static final int REVEAL_TICKS = 30;

        private int firstCol = -1;
        private int firstRow = -1;
        private boolean resolving = false;

        private final Map<UUID, Integer> score = new HashMap<>();

        public MemoryGame(Player p1, Player p2) {
            super(p1, p2, 4, 4);
            values = new int[getCols()][getRows()];
            state  = new int[getCols()][getRows()];
            generate();
        }

        @Override
        public String getTitle() {
            return LangManager.get().get(CoreLang.COMMON_MINIGAME_TITLE_MEMORY, Map.of(
                    "%game%", MiniGames.MiniGameType.MEMORY.getDisplayName(),
                    "%score1%", score.getOrDefault(getP1(), 0),
                    "%score2%", score.getOrDefault(getP2(), 0)));
        }

        private void generate() {
            List<Integer> pool = new ArrayList<>();
            int size = getCols() * getRows();
            for (int i = 0; i < size / 2; i++) { pool.add(i); pool.add(i); }
            Collections.shuffle(pool);
            int i = 0;
            for (int x = 0; x < getCols(); x++)
                for (int y = 0; y < getRows(); y++)
                    values[x][y] = pool.get(i++);
        }

        @Override
        public boolean play(UUID uuid, int col, int row) {
            if (getState() != State.ONGOING) return false;
            if (resolving)                      return false;
            if (!uuid.equals(currentPlayer()))  return false;
            if (state[col][row] != 0)           return false;

            state[col][row] = 1;
            getGrid()[cell(col, row)] = values[col][row];

            if (firstCol == -1) {
                firstCol = col;
                firstRow = row;
                refreshViews();
                return true;
            }

            int v1 = values[firstCol][firstRow];
            int v2 = values[col][row];
            int fc = firstCol, fr = firstRow;
            firstCol = -1;
            firstRow = -1;

            if (v1 == v2) {
                state[fc][fr] = 2;
                state[col][row] = 2;
                score.merge(uuid, 1, Integer::sum);

                if (allMatched()) {
                    int s1 = score.getOrDefault(getP1(), 0);
                    int s2 = score.getOrDefault(getP2(), 0);
                    if      (s1 > s2) forceWin(1);
                    else if (s2 > s1) forceWin(2);
                    else              forceDraw();
                } else {
                    refreshViews();
                }
            } else {
                resolving = true;
                refreshViews();
                Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
                    if (getState() != State.ONGOING) return;
                    state[fc][fr] = 0;
                    state[col][row] = 0;
                    getGrid()[cell(fc, fr)] = 0;
                    getGrid()[cell(col, row)] = 0;
                    nextTurn();
                    resolving = false;
                    refreshViews();
                }, REVEAL_TICKS);
            }

            return true;
        }

        private boolean allMatched() {
            for (int x = 0; x < getCols(); x++)
                for (int y = 0; y < getRows(); y++)
                    if (state[x][y] != 2) return false;
            return true;
        }

        @Override
        public ItemStack cellItem(int value, int col, int row) {
            if (state[col][row] == 0) {
                return MiniGames.pane((short) 15, "§8?");
            }
            short dur  = CARD_COLORS[values[col][row] % CARD_COLORS.length];
            boolean matched = state[col][row] == 2;
            String name = (matched ? "§a§l✓ §r" : "§f") + (char)('A' + values[col][row]);
            return MiniGames.pane(dur, name);
        }

        public int getScore(Player p) {
            return score.getOrDefault(p.getUniqueId(), 0);
        }
    }

    public static class ShifumiGame implements MiniGames.MiniGame {

        public enum Choice {
            PIERRE(11, Material.STONE, CoreLang.COMMON_MINIGAME_SHIFUMI_ROCK),
            FEUILLE(13, Material.PAPER, CoreLang.COMMON_MINIGAME_SHIFUMI_PAPER),
            CISEAUX(15, Material.SHEARS, CoreLang.COMMON_MINIGAME_SHIFUMI_SCISSORS);

            private final int slot;
            private final Material material;
            private final CoreLang nameKey;

            Choice(int slot, Material material, CoreLang nameKey) {
                this.slot = slot;
                this.material = material;
                this.nameKey = nameKey;
            }

            public int getSlot() {
                return slot;
            }

            public Material getMaterial() {
                return material;
            }

            public CoreLang getNameKey() {
                return nameKey;
            }

            public boolean beats(Choice other) {
                return (this == PIERRE && other == CISEAUX)
                        || (this == CISEAUX && other == FEUILLE)
                        || (this == FEUILLE && other == PIERRE);
            }
        }

        private final UUID p1;
        private final UUID p2;
        private final String name1;
        private final String name2;
        private final Map<UUID, Choice> choices = new HashMap<>();
        private final Map<UUID, ShifumiView> views = new HashMap<>();
        private boolean running = true;

        public ShifumiGame(Player p1, Player p2) {
            this.p1 = p1.getUniqueId();
            this.p2 = p2.getUniqueId();
            this.name1 = p1.getName();
            this.name2 = p2.getName();
        }

        @Override
        public void start() {
            MiniGames.MiniGameRegistry.register(this);
            openFor(p1);
            openFor(p2);
        }

        @Override
        public void stop() {
            running = false;
            MiniGames.MiniGameRegistry.unregister(this);
            closeViewsLater();
        }

        @Override
        public void onPlayerQuit(UUID uuid) {
            if (running) {
                UUID other = p1.equals(uuid) ? p2 : p2.equals(uuid) ? p1 : null;
                Player winner = other == null ? null : Bukkit.getPlayer(other);
                if (winner != null) {
                    LangManager.get().send(CoreLang.COMMON_MINIGAME_WIN, winner);
                }
            }
            stop();
        }

        public void forfeit(UUID uuid) {
            if (!running) return;
            UUID other = p1.equals(uuid) ? p2 : p2.equals(uuid) ? p1 : null;
            if (other == null) return;
            Player winner = Bukkit.getPlayer(other);
            Player loser = Bukkit.getPlayer(uuid);
            if (winner != null) LangManager.get().send(CoreLang.COMMON_MINIGAME_WIN, winner);
            if (loser != null) LangManager.get().send(CoreLang.COMMON_MINIGAME_LOSE, loser);
            stop();
        }

        @Override
        public Collection<UUID> participants() {
            return List.of(p1, p2);
        }

        public boolean isRunning() {
            return running;
        }

        public Choice choiceOf(UUID uuid) {
            return choices.get(uuid);
        }

        public void choose(UUID uuid, Choice choice) {
            if (!running) return;
            if (!p1.equals(uuid) && !p2.equals(uuid)) return;
            if (choices.containsKey(uuid)) return;
            choices.put(uuid, choice);
            refreshView(uuid);
            if (choices.containsKey(p1) && choices.containsKey(p2)) {
                resolve();
            }
        }

        private void resolve() {
            Choice c1 = choices.get(p1);
            Choice c2 = choices.get(p2);
            Player player1 = Bukkit.getPlayer(p1);
            Player player2 = Bukkit.getPlayer(p2);

            sendReveal(player1, c1, c2);
            sendReveal(player2, c1, c2);

            if (c1 == c2) {
                LangManager.get().send(CoreLang.COMMON_MINIGAME_DRAW, player1);
                LangManager.get().send(CoreLang.COMMON_MINIGAME_DRAW, player2);
                choices.clear();
                refreshView(p1);
                refreshView(p2);
                return;
            }

            boolean firstWins = c1.beats(c2);
            Player winner = firstWins ? player1 : player2;
            Player loser = firstWins ? player2 : player1;
            LangManager.get().send(CoreLang.COMMON_MINIGAME_WIN, winner);
            LangManager.get().send(CoreLang.COMMON_MINIGAME_LOSE, loser);
            stop();
        }

        private void sendReveal(Player viewer, Choice c1, Choice c2) {
            if (viewer == null || !viewer.isOnline()) return;
            LangManager.get().send(CoreLang.COMMON_MINIGAME_SHIFUMI_REVEAL, viewer,
                    Map.of("%player%", name1, "%choice%", LangManager.get().get(c1.getNameKey(), viewer)));
            LangManager.get().send(CoreLang.COMMON_MINIGAME_SHIFUMI_REVEAL, viewer,
                    Map.of("%player%", name2, "%choice%", LangManager.get().get(c2.getNameKey(), viewer)));
        }

        private void openFor(UUID uuid) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) return;
            views.computeIfAbsent(uuid, u -> new ShifumiView(player, this)).open();
        }

        private void refreshView(UUID uuid) {
            ShifumiView view = views.get(uuid);
            if (view == null) {
                openFor(uuid);
                return;
            }
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) return;
            view.open();
        }

        private void closeViewsLater() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    closeFor(p1);
                    closeFor(p2);
                }
            }.runTaskLater(Main.get(), 1);
        }

        private void closeFor(UUID uuid) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) return;
            ShifumiView view = views.get(uuid);
            if (view == null || CustomInventory.cache.get(uuid) != view) return;
            player.closeInventory();
        }

        public static class ShifumiView extends CustomInventory {

            private final ShifumiGame game;

            public ShifumiView(Player player, ShifumiGame game) {
                super(player);
                this.game = game;
            }

            @Override
            public String getTitle() {
                return MiniGames.MiniGameType.SHIFUMI.getTitle();
            }

            @Override
            public int getLines() {
                return 3;
            }

            @Override
            public boolean isRefreshAuto() {
                return false;
            }

            @Override
            public void onClose() {
                if (!game.isRunning() || !getPlayer().isOnline()) return;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (game.isRunning() && getPlayer().isOnline()) {
                            open();
                        }
                    }
                }.runTaskLater(Main.get(), 1);
            }

            @Override
            public void setup() {
                for (int slot = 0; slot < getLines() * 9; slot++) {
                    if (isChoiceSlot(slot)) continue;
                    addItem(new StaticItem(slot, MiniGames.filler()));
                }

                ShifumiGame.Choice chosen = game.choiceOf(getPlayer().getUniqueId());

                if (chosen != null) {
                    addItem(new StaticItem(infoSlot(), new ItemCreator(Material.PAPER)
                            .setName(t(CoreLang.COMMON_MINIGAME_SHIFUMI_WAITING))));
                }

                for (ShifumiGame.Choice choice : ShifumiGame.Choice.values()) {
                    String name = t(choice.getNameKey());
                    if (choice == chosen) {
                        name = "§a✔ " + name;
                    }
                    addItem(new ActionItem(choice.getSlot(),
                            new ItemCreator(choice.getMaterial()).setName(name).getItemstack()) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            game.choose(getPlayer().getUniqueId(), choice);
                        }
                    });
                }

                addItem(new ActionItem(quitSlot(),
                        new ItemCreator(Material.BARRIER).setName(t(CoreLang.COMMON_MINIGAME_QUIT)).getItemstack()) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        game.forfeit(getPlayer().getUniqueId());
                    }
                });
            }

            private int quitSlot() {
                return 26;
            }

            private boolean isChoiceSlot(int slot) {
                for (ShifumiGame.Choice choice : ShifumiGame.Choice.values()) {
                    if (choice.getSlot() == slot) return true;
                }
                return false;
            }

            private int infoSlot() {
                return 4;
            }
        }
    }

    public static class SnakeGame extends RealTimeGame {

        private static final int COLS = 9;

        private final int rows;
        private final long tickPeriod;
        private final Random random = new Random();

        private final Deque<Integer> snake = new ArrayDeque<>();
        private int dx = 1;
        private int dy = 0;
        private int pendingDx = 1;
        private int pendingDy = 0;
        private int apple = -1;
        private int score = 0;

        public SnakeGame(Player player) {
            this(player, 4, 8L);
        }

        public SnakeGame(Player player, int rows, long tickPeriod) {
            super(player);
            this.rows = Math.min(5, Math.max(2, rows));
            this.tickPeriod = Math.max(2L, tickPeriod);
        }

        @Override
        public String getTitle() {
            return MiniGames.MiniGameType.SNAKE.getTitle();
        }

        @Override
        public int getLines() {
            return rows + 1;
        }

        @Override
        public boolean isRefreshAuto() {
            return false;
        }

        public int getScore() {
            return score;
        }

        public void onGameOver(Player player, int finalScore) {
            if (player != null && player.isOnline()) {
                LangManager.get().send(CoreLang.COMMON_MINIGAME_SNAKE_OVER, player, Map.of("%score%", finalScore));
                player.playSound(player.getLocation(), Sound.NOTE_BASS, 1f, 0.6f);
            }
        }

        @Override
        protected long tickPeriod() {
            return tickPeriod;
        }

        @Override
        protected void onStart() {
            snake.clear();
            snake.addFirst(cell(COLS / 2, rows / 2));
            dx = 1;
            dy = 0;
            pendingDx = 1;
            pendingDy = 0;
            score = 0;
            spawnApple();
        }

        @Override
        protected void onFinish() {
            redraw();
            onGameOver(getPlayer(), score);
        }

        @Override
        public void setup() {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < COLS; col++) {
                    addItem(new StaticItem(cell(col, row), boardItem(cell(col, row))));
                }
            }

            int controls = rows * 9;
            addItem(new StaticItem(controls + 4,
                    new ItemCreator(Material.PAPER).setName(t(CoreLang.COMMON_MINIGAME_SNAKE_SCORE, Map.of("%score%", score)))));
            addDirection(controls + 1, "§f◀", -1, 0);
            addDirection(controls + 3, "§f▲", 0, -1);
            addDirection(controls + 5, "§f▼", 0, 1);
            addDirection(controls + 7, "§f▶", 1, 0);

            for (int slot : new int[]{controls, controls + 2, controls + 6, controls + 8}) {
                addItem(new StaticItem(slot, MiniGames.filler()));
            }
        }

        private void addDirection(int slot, String name, int ddx, int ddy) {
            addItem(new ActionItem(slot, MiniGames.pane((short) 11, name)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    if (over) return;
                    if (snake.size() > 1 && ddx == -dx && ddy == -dy) return;
                    pendingDx = ddx;
                    pendingDy = ddy;
                }
            });
        }

        @Override
        protected void tick() {
            dx = pendingDx;
            dy = pendingDy;

            int head = snake.peekFirst();
            int col = head % COLS + dx;
            int row = head / COLS + dy;

            if (col < 0 || col >= COLS || row < 0 || row >= rows) {
                gameOver();
                return;
            }

            int newHead = cell(col, row);
            boolean eats = newHead == apple;

            if (!eats) snake.removeLast();
            if (snake.contains(newHead)) {
                gameOver();
                return;
            }
            snake.addFirst(newHead);

            if (eats) {
                score++;
                getPlayer().playSound(getPlayer().getLocation(), Sound.ORB_PICKUP, 0.7f, 1.4f);
                spawnApple();
            }

            redraw();
        }

        private void gameOver() {
            finish();
        }

        private void spawnApple() {
            List<Integer> free = new ArrayList<>();
            for (int i = 0; i < rows * COLS; i++) {
                if (!snake.contains(i)) free.add(i);
            }
            apple = free.isEmpty() ? -1 : free.get(random.nextInt(free.size()));
            if (apple == -1) gameOver();
        }

        private void redraw() {
            Player player = getPlayer();
            if (!player.isOnline()) return;
            if (CustomInventory.cache.get(player.getUniqueId()) != this) return;
            Inventory top = player.getOpenInventory() == null ? null : player.getOpenInventory().getTopInventory();
            if (top == null || top.getSize() != getLines() * 9) return;

            for (int i = 0; i < rows * COLS; i++) {
                top.setItem(i, boardItem(i));
            }
            top.setItem(rows * 9 + 4, new ItemCreator(Material.PAPER)
                    .setName(t(CoreLang.COMMON_MINIGAME_SNAKE_SCORE, Map.of("%score%", score))).getItemstack());
            player.updateInventory();
        }

        private ItemStack boardItem(int idx) {
            short durability;
            String name = " ";
            if (idx == apple) {
                durability = 14;
                name = "§c❤";
            } else if (!snake.isEmpty() && snake.peekFirst() == idx) {
                durability = over ? (short) 1 : (short) 13;
                name = "§a■";
            } else if (snake.contains(idx)) {
                durability = 5;
                name = "§a■";
            } else {
                durability = 15;
            }
            return MiniGames.pane(durability, name);
        }

        private int cell(int col, int row) {
            return row * COLS + col;
        }
    }

    public static class MorpionGame extends TurnBasedGridGame {

        private final int align;

        public MorpionGame(Player p1, Player p2) {
            this(p1, p2, 3, 3);
        }

        public MorpionGame(Player p1, Player p2, int size, int align) {
            super(p1, p2, size, size);
            this.align = Math.max(2, Math.min(align, size));
        }

        @Override
        public String getTitle() {
            return MiniGames.MiniGameType.MORPION.getTitle();
        }

        @Override
        protected State checkEnd(int lastPlayer) {
            return alignmentEnd(lastPlayer, align);
        }
    }

    public static class Puissance4Game extends TurnBasedGridGame {

        private static final int ALIGN = 4;

        public Puissance4Game(Player p1, Player p2) {
            this(p1, p2, 7, 5);
        }

        public Puissance4Game(Player p1, Player p2, int cols, int rows) {
            super(p1, p2, cols, rows);
        }

        @Override
        public String getTitle() {
            return MiniGames.MiniGameType.PUISSANCE4.getTitle();
        }

        @Override
        protected int placementCell(int col, int row) {
            for (int r = getRows() - 1; r >= 0; r--) {
                if (cellValue(col, r) == 0) return cell(col, r);
            }
            return -1;
        }

        @Override
        protected State checkEnd(int lastPlayer) {
            return alignmentEnd(lastPlayer, ALIGN);
        }
    }
}