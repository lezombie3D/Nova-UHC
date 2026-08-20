package net.novaproject.novauhc.ui;

import java.util.ArrayList;
import java.util.List;

public final class MenuGrid {

    private MenuGrid() {
    }

    public static int pages(int contentSize, int perPage) {
        return perPage <= 0 ? 1 : Math.max(1, (contentSize + perPage - 1) / perPage);
    }

    public static int gridCapacity(int rowFrom, int rowTo, int colFrom, int colTo) {
        return (rowTo - rowFrom + 1) * (colTo - colFrom + 1);
    }

    public static int spacedCapacity(int rowFrom, int rowTo, int colFrom, int colTo) {
        return (rowTo - rowFrom + 1) * ((colTo - colFrom + 2) / 2);
    }

    public static int[] grid(int rowFrom, int rowTo, int colFrom, int colTo, boolean centered, int count) {
        int perRow = colTo - colFrom + 1;
        List<Integer> out = new ArrayList<>();
        int placed = 0;
        for (int row = rowFrom; row <= rowTo && placed < count; row++) {
            int inRow = Math.min(perRow, count - placed);
            int startCol = centered ? colFrom + (perRow - inRow) / 2 : colFrom;
            for (int j = 0; j < inRow; j++) out.add(row * 9 + startCol + j);
            placed += inRow;
        }
        return toArray(out);
    }

    public static int[] spaced(int rowFrom, int rowTo, int colFrom, int colTo, int count) {
        int perRow = (colTo - colFrom + 2) / 2;
        List<Integer> out = new ArrayList<>();
        int placed = 0;
        for (int row = rowFrom; row <= rowTo && placed < count; row++) {
            int inRow = Math.min(perRow, count - placed);
            int span = 2 * inRow - 1;
            int startCol = (9 - span) / 2;
            for (int j = 0; j < inRow; j++) out.add(row * 9 + startCol + 2 * j);
            placed += inRow;
        }
        return toArray(out);
    }

    public static int[] linear(int from, int to, int count) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < count && from + i <= to; i++) out.add(from + i);
        return toArray(out);
    }

    public static int[] rect(int columns, int rowOffset, int count) {
        int originCol = (9 - columns) / 2;
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            out.add((i / columns + rowOffset) * 9 + originCol + i % columns);
        }
        return toArray(out);
    }

    private static int[] toArray(List<Integer> list) {
        return list.stream().mapToInt(Integer::intValue).toArray();
    }
}
