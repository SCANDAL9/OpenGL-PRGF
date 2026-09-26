package com.graphics.city;

public final class CityMap {
    public static final int STREET = 0;
    public static final int BLOCK = 1;
    public static final int BUILDING = 2;

    private static final int[][] CELLS = {
        {STREET, STREET, STREET, STREET, STREET, STREET, STREET},
        {STREET, BLOCK, STREET, BLOCK, STREET, BLOCK, STREET},
        {STREET, STREET, STREET, STREET, STREET, STREET, STREET},
        {STREET, BUILDING, STREET, BLOCK, STREET, BLOCK, STREET},
        {STREET, STREET, STREET, STREET, STREET, STREET, STREET},
        {STREET, BLOCK, STREET, BUILDING, STREET, BLOCK, STREET},
        {STREET, STREET, STREET, STREET, STREET, STREET, STREET}
    };

    public int[][] cells() {
        return CELLS;
    }

    public int valueAt(int row, int column) {
        return CELLS[row][column];
    }

    public int rows() { return CELLS.length; }
    public int columns() { return CELLS[0].length; }
}
