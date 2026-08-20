package net.novaproject.novauhc.minigame;

import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class TurnBasedGridGame implements MiniGames.MiniGame {

    public enum State { ONGOING, P1_WIN, P2_WIN, DRAW }

    private static final short PLAYER_SKULL_DATA = 3;
    private static final float TURN_SOUND_PITCH = 1.6f;

    private final UUID p1;
    private final UUID p2;
    private final int cols;
    private final int rows;
    private final int[] grid;
    private int turn = 1;
    private State state = State.ONGOING;
    private UUID lastAnnouncedTurn = null;

    private final Map<UUID, GridGameView> views = new HashMap<>();

    protected TurnBasedGridGame(Player p1, Player p2, int cols, int rows) {
        this.p1 = p1.getUniqueId();
        this.p2 = p2.getUniqueId();
        this.cols = Math.min(9, Math.max(1, cols));
        this.rows = Math.min(6, Math.max(1, rows));
        this.grid = new int[this.cols * this.rows];
    }

    public abstract String getTitle();

    protected State checkEnd(int lastPlayer) {
        return State.ONGOING;
    }

    protected final State alignmentEnd(int lastPlayer, int align) {
        if (GridWinChecker.hasAlignment(grid, cols, rows, lastPlayer, align)) {
            return lastPlayer == 1 ? State.P1_WIN : State.P2_WIN;
        }
        return State.ONGOING;
    }

    public void onEnd(State endState, Player winner, Player loser) {
        if (endState == State.DRAW) {
            if (winner != null) LangManager.get().send(CoreLang.COMMON_MINIGAME_DRAW, winner);
            if (loser != null) LangManager.get().send(CoreLang.COMMON_MINIGAME_DRAW, loser);
            return;
        }
        if (winner != null) LangManager.get().send(CoreLang.COMMON_MINIGAME_WIN, winner);
        if (loser != null) LangManager.get().send(CoreLang.COMMON_MINIGAME_LOSE, loser);
    }

    protected int placementCell(int col, int row) {
        return cell(col, row);
    }

    public ItemStack cellItem(int value, int col, int row) {
        short durability = value == 1 ? (short) 14 : value == 2 ? (short) 4 : (short) 7;
        String name = value == 0 ? " " : value == 1 ? "§c●" : "§e●";
        return MiniGames.pane(durability, name);
    }

    public final int getCols() { return cols; }
    public final int getRows() { return rows; }
    public final int[] getGrid() { return grid; }
    public final int getTurn() { return turn; }
    public final State getState() { return state; }
    public final UUID getP1() { return p1; }
    public final UUID getP2() { return p2; }

    public final int cell(int col, int row) {
        return row * cols + col;
    }

    public final int cellValue(int col, int row) {
        int idx = cell(col, row);
        return idx < 0 || idx >= grid.length ? -1 : grid[idx];
    }

    public final UUID currentPlayer() {
        if (state != State.ONGOING) return null;
        return turn == 1 ? p1 : p2;
    }

    public final int playerNumber(UUID uuid) {
        if (p1.equals(uuid)) return 1;
        if (p2.equals(uuid)) return 2;
        return 0;
    }

    protected final void nextTurn() {
        turn = turn == 1 ? 2 : 1;
    }

    public ItemStack cellItemFor(int value, int col, int row, Player viewer) {
        return cellItem(value, col, row);
    }
    public boolean play(UUID uuid, int col, int row) {
        if (state != State.ONGOING) return false;
        if (col < 0 || col >= cols || row < 0 || row >= rows) return false;
        if (!uuid.equals(currentPlayer())) return false;

        int idx = placementCell(col, row);
        if (idx < 0 || idx >= grid.length || grid[idx] != 0) return false;

        grid[idx] = turn;

        State end = checkEnd(turn);
        if (end != State.ONGOING) {
            state = end;
        } else if (isFull()) {
            state = State.DRAW;
        } else {
            turn = turn == 1 ? 2 : 1;
        }

        refreshViews();

        if (state != State.ONGOING) {
            finish();
        }
        return true;
    }

    public final void forceWin(int who) {
        if (state != State.ONGOING) return;
        state = who == 1 ? State.P1_WIN : State.P2_WIN;
        refreshViews();
        finish();
    }

    public final void forceDraw() {
        if (state != State.ONGOING) return;
        state = State.DRAW;
        refreshViews();
        finish();
    }

    public final boolean isFull() {
        for (int v : grid) if (v == 0) return false;
        return true;
    }

    @Override
    public final void start() {
        MiniGames.MiniGameRegistry.register(this);
        openFor(p1);
        openFor(p2);
        announceTurn();
    }

    @Override
    public void stop() {
        MiniGames.MiniGameRegistry.unregister(this);
    }

    @Override
    public void onPlayerQuit(UUID uuid) {

        if (state == State.ONGOING) {
            int who = playerNumber(uuid);
            if (who == 1) {
                forceWin(2);
                return;
            }
            if (who == 2) {
                forceWin(1);
                return;
            }
        }
        stop();
    }

    @Override
    public Collection<UUID> participants() {
        return List.of(p1, p2);
    }

    public final void openFor(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) return;
        GridGameView view = views.computeIfAbsent(uuid, u -> new GridGameView(player, this));
        view.open();
    }

    public final void refreshViews() {
        for (UUID uuid : views.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            views.get(uuid).open();
        }
        announceTurn();
    }

    private void announceTurn() {
        UUID current = currentPlayer();
        if (current == null || current.equals(lastAnnouncedTurn)) return;
        lastAnnouncedTurn = current;
        Player player = Bukkit.getPlayer(current);
        if (player == null || !player.isOnline()) return;
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, TURN_SOUND_PITCH);
    }

    private void finish() {
        Player winner = null;
        Player loser = null;
        if (state == State.P1_WIN) {
            winner = Bukkit.getPlayer(p1);
            loser = Bukkit.getPlayer(p2);
        } else if (state == State.P2_WIN) {
            winner = Bukkit.getPlayer(p2);
            loser = Bukkit.getPlayer(p1);
        } else {
            winner = Bukkit.getPlayer(p1);
            loser = Bukkit.getPlayer(p2);
        }
        onEnd(state, winner, loser);
        MiniGames.MiniGameRegistry.unregister(this);
    }

    public static class GridGameView extends CustomInventory {

        private final TurnBasedGridGame game;

        public GridGameView(Player player, TurnBasedGridGame game) {
            super(player);
            this.game = game;
        }

        @Override
        public String getTitle() {
            return game.getTitle();
        }

        @Override
        public int getLines() {
            return game.getRows() + 1;
        }

        @Override
        public boolean isRefreshAuto() {
            return false;
        }

        @Override
        public void onClose() {

            if (game.getState() == TurnBasedGridGame.State.ONGOING
                    && getPlayer().isOnline()) {
                open(1);
            }
        }

        private int originCol() {
            return (9 - game.getCols()) / 2;
        }

        private int boardRowOffset() {
            return 1;
        }

        @Override
        public void setup() {
            int originCol = originCol();
            int rowOffset = boardRowOffset();

            for (int slot = 0; slot < getLines() * 9; slot++) {
                int col = slot % 9;
                int row = slot / 9;
                boolean onBoard = row >= rowOffset && row < rowOffset + game.getRows()
                        && col >= originCol && col < originCol + game.getCols();
                if (!onBoard) {
                    addItem(new StaticItem(slot, MiniGames.filler()));
                }
            }

            addItem(new StaticItem(infoSlot(), turnHead()));

            for (int row = 0; row < game.getRows(); row++) {
                for (int col = 0; col < game.getCols(); col++) {
                    final int c = col;
                    final int r = row;
                    int slot = (row + rowOffset) * 9 + originCol + col;
                    addItem(new ActionItem(slot, game.cellItemFor(game.cellValue(col, row), col, row, getPlayer())) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            game.play(getPlayer().getUniqueId(), c, r);
                        }
                    });
                }
            }
        }

        private int infoSlot() {
            return 4;
        }

        private ItemCreator turnHead() {
            UUID current = game.currentPlayer();
            Player holder = current == null ? null : Bukkit.getPlayer(current);
            if (holder == null) {
                return new ItemCreator(Material.PAPER).setName(turnText());
            }
            return new ItemCreator(Material.SKULL_ITEM)
                    .setDurability(PLAYER_SKULL_DATA)
                    .setOwner(holder.getName())
                    .setName(turnText());
        }

        private String turnText() {
            TurnBasedGridGame.State state = game.getState();
            if (state == TurnBasedGridGame.State.DRAW) return t(CoreLang.COMMON_MINIGAME_DRAW);
            if (state == TurnBasedGridGame.State.P1_WIN || state == TurnBasedGridGame.State.P2_WIN) {
                int winner = state == TurnBasedGridGame.State.P1_WIN ? 1 : 2;
                boolean me = game.playerNumber(getPlayer().getUniqueId()) == winner;
                return me ? t(CoreLang.COMMON_MINIGAME_WIN) : t(CoreLang.COMMON_MINIGAME_LOSE);
            }
            UUID current = game.currentPlayer();
            if (current != null && current.equals(getPlayer().getUniqueId())) {
                return t(CoreLang.COMMON_MINIGAME_YOUR_TURN);
            }
            Player opponent = current == null ? null : Bukkit.getPlayer(current);
            return t(CoreLang.COMMON_MINIGAME_OPPONENT_TURN,
                    Map.of("%player%", opponent == null ? "?" : opponent.getName()));
        }
    }

    public static final class GridWinChecker {


        public static boolean hasAlignment(int[] grid, int cols, int rows, int player, int align) {
            int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    if (grid[row * cols + col] != player) continue;
                    for (int[] dir : directions) {
                        int count = 1;
                        int c = col + dir[0];
                        int r = row + dir[1];
                        while (c >= 0 && c < cols && r >= 0 && r < rows && grid[r * cols + c] == player) {
                            count++;
                            if (count >= align) return true;
                            c += dir[0];
                            r += dir[1];
                        }
                    }
                }
            }
            return false;
        }
    }
}
