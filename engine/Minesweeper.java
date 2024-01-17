package engine;

import engine.utils.Cell;
import engine.utils.CellType;
import engine.utils.Tuple;

import java.util.Random;

public class Minesweeper {
    private final Cell[][] matrix;
    private final boolean[][] uncovered;
    private final boolean[][] highlightedMines;
    private static final Random random = new Random();
    private int uncoverCount = 0;
    private final int mines;

    public Minesweeper(int width, int height, int mines) {
        matrix = new Cell[width][height];
        uncovered = new boolean[width][height];
        highlightedMines = new boolean[width][height];
        // Initialize the matrix and uncovered arrays
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                matrix[x][y] = new Cell(CellType.EMPTY);
                uncovered[x][y] = false;
            }
        }
        // The array doesn't get generated here but when the first cell is uncovered instead
        this.mines = mines;
    }

    /**
     * Populates the minefield with the mines.
     */
    private void placeMines(){
        for (int i = 0; i < mines; i++) {
            int randomX, randomY;

            do {
                randomX = random.nextInt(0, this.getFieldWidth());
                randomY = random.nextInt(0, this.getFieldHeight());

            } while (matrix[randomX][randomY].type == CellType.MINE);

            matrix[randomX][randomY] = new Cell(CellType.MINE);
        }
    }

    /**
     * Populates the minefield with the numbers.
     */
    private void placeNumbers(){
        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix[0].length; y++) {
                int adjacentMines = getNumbersOfMines(x, y);
                if (adjacentMines != 0 && !isMine(x, y)){
                    matrix[x][y] = new Cell(CellType.NUMBER);
                    matrix[x][y].setAdjacentMines(adjacentMines);
                }
            }
        }
    }
    /**
     * @return The width of the minefield.
     */
    public int getFieldWidth(){
        return matrix.length;
    }
    /**
     * @return The height of the minefield.
     */
    public int getFieldHeight(){
        return matrix[0].length;
    }

    /**
     * Returns the minefield as a string.
     *
     * @return A string representation of the minefield.
     */
    public String getFieldAsString() {
        StringBuilder res = new StringBuilder();

        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix[0].length; y++) {
                if (isUncovered(x, y)) {
                    res.append(matrix[x][y].getChar());
                } else {
                    res.append(Cell.getCharFor(CellType.HIDDEN));
                }
                // Add extra space at the end
                if (matrix[0].length-1 != y){
                    res.append(" ");
                }
            }
            res.append("\n");  // Append newline after each row
        }

        return res.toString();
    }

    /**
     * Uncovers the specified cell in the Minesweeper game.
     *
     * <p>This method uncovers the specified cell in the Minesweeper game and returns the result as a tuple.
     * The first element of the tuple is the character representing the uncovered cell.
     * The second element of the tuple is another tuple containing the score obtained from uncovering the cell and a boolean
     * indicating whether the game has ended.
     * The score is the total score obtained from uncovering the specified cell and any adjacent cells.
     * The game is considered ended if all non-mine cells are uncovered.</p>
     *
     * <p>If the specified cell is flagged, the method returns a tuple with the first element as null, indicating that
     * the cell was not uncovered. The score and game-ended status are set to 0 and false, respectively.</p>
     *
     * @param x The x-coordinate of the cell to be uncovered.
     * @param y The y-coordinate of the cell to be uncovered.
     * @return A tuple containing the uncovered cell's character, the score, and a flag indicating whether the game has ended.
     */
    public Tuple<Character, Tuple<Integer, Boolean>> uncover(int x, int y) {
        if (isCellHighlighted(x, y)){
            return new Tuple<>(null, new Tuple<>(0, false));
        }
        boolean wasUncovered;
        CellType cellValue = CellType.NOT_SET;
        int score = 0;
        boolean gameEnded = true;

        try {
            // The first time the player uncovers a tile must always be a safe tile
            if (uncoverCount == 0){
                // Place mines randomly on the matrix
                placeMines();
                // Force the mined cell to be safe
                matrix[y][x] = new Cell(CellType.EMPTY);
                // Place numbers
                placeNumbers();
            }
            uncoverCount++;
            wasUncovered = uncovered[y][x];
            uncovered[y][x] = true;
            cellValue = matrix[y][x].type;
            // If the cell wasn't already uncovered
            if (!wasUncovered){
                score++;

                if (cellValue == CellType.EMPTY) {
                    // Check and uncover adjacent cells recursively
                    score += uncoverAdjacent(x - 1, y);
                    score += uncoverAdjacent(x + 1, y);
                    score += uncoverAdjacent(x, y - 1);
                    score += uncoverAdjacent(x, y + 1);
                }
            }

            for (x = 0; x < matrix.length; x++) {
                for (y = 0; y < matrix[0].length; y++) {
                    if (!isUncovered(x, y) && matrix[x][y].type != CellType.MINE){
                        gameEnded = false;
                        break;
                    }
                }
                if (!gameEnded){
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignore) {
            // Ignore if the coordinates are out of bounds
        }

        return new Tuple<>(Cell.getCharFor(cellValue), new Tuple<>(score, gameEnded));
    }

    /**
     * Uncovers the adjacent cells around the specified coordinates if they are not already uncovered.
     *
     * <p>This method uncovers the adjacent cells around the specified coordinates in the Minesweeper game
     * if they are not already uncovered and not containing a mine. The method returns the score obtained
     * from uncovering the specified cell. If the coordinates are out of bounds or the cell contains a mine,
     * the method returns 0.</p>
     *
     * @param x The x-coordinate of the cell around which adjacent cells should be uncovered.
     * @param y The y-coordinate of the cell around which adjacent cells should be uncovered.
     * @return The score obtained from uncovering the specified cell and its adjacent cells.
     */
    private int uncoverAdjacent(int x, int y) {
        try {
            if (!uncovered[y][x] && matrix[y][x].type != CellType.MINE) {
                return uncover(x, y).second().first();
            }
        } catch (ArrayIndexOutOfBoundsException ignore) {}
        return 0;
    }

    /**
     * Checks if the cell at the specified coordinates contains a mine.
     *
     * <p>This method checks whether the cell at the specified coordinates in the Minesweeper game
     * contains a mine. If the coordinates are out of bounds, the method returns false.</p>
     *
     * @param x The x-coordinate of the cell to be checked for a mine.
     * @param y The y-coordinate of the cell to be checked for a mine.
     * @return {@code true} if the cell contains a mine, {@code false} otherwise.
     */
    private boolean isMine(int x, int y) {
        try {
            return matrix[x][y].type == CellType.MINE;
        } catch (ArrayIndexOutOfBoundsException ignore) {
            // Ignore if the coordinates are out of bounds
        }
        return false;
    }

    /**
     * Gets the number of mines in neighboring cells around the specified coordinates.
     *
     * <p>This method calculates and returns the number of mines in the neighboring cells
     * around the specified coordinates in the Minesweeper game. If the coordinates are
     * out of bounds, the method returns 0.
     * Checks in all the 8 directions.</p>
     *
     * @param x The x-coordinate of the cell for which the number of neighboring mines is calculated.
     * @param y The y-coordinate of the cell for which the number of neighboring mines is calculated.
     * @return The number of mines in neighboring cells.
     */
    private int getNumbersOfMines(int x, int y) {
        int num = 0;

        // loop the 8 adjacent cells
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if ((i != x || j != y) && isMine(i, j)) {
                    num++;
                }
            }
        }

        return num;
    }

    /**
     * Gets the number of mines in neighboring cells around the specified coordinates.
     *
     * <p>This method calculates and returns the number of mines in the neighboring cells
     * around the specified coordinates in the Minesweeper game. If the coordinates are
     * out of bounds, the method returns 0.
     * Checks in all the 8 directions.</p>
     *
     * @param x The x-coordinate of the cell for which the number of neighboring mines is calculated.
     * @param y The y-coordinate of the cell for which the number of neighboring mines is calculated.
     * @return The number of mines in neighboring cells.
     */
    public int getNumbersOfFlaggedCells(int x, int y) {
        int num = 0;

        // loop the 8 adjacent cells
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if ((i != x || j != y) && isCellHighlighted(j, i)) {
                    num++;
                }
            }
        }

        return num;
    }

    /**
     * Checks if the cell at the specified coordinates is highlighted.
     *
     * <p>This method checks whether the cell at the specified coordinates in the Minesweeper game
     * is currently highlighted. If the coordinates are out of bounds, the method returns false.</p>
     *
     * @param x The x-coordinate of the cell to be checked for highlighting.
     * @param y The y-coordinate of the cell to be checked for highlighting.
     * @return {@code true} if the cell is highlighted, {@code false} otherwise.
     */
    public boolean isCellHighlighted(int x, int y) {
        try {
            return highlightedMines[x][y];
        } catch (ArrayIndexOutOfBoundsException ignore) {
            // Ignore if the coordinates are out of bounds
            return false;
        }
    }

    /**
     * Highlights the cell at the specified coordinates.
     *
     * <p>This method highlights the cell at the specified coordinates in the Minesweeper game.
     * If the coordinates are out of bounds, the method ignores the operation.</p>
     *
     * @param x The x-coordinate of the cell to be highlighted.
     * @param y The y-coordinate of the cell to be highlighted.
     */
    private void highlightCell(int x, int y) {
        try {
            highlightedMines[x][y] = true;
        } catch (ArrayIndexOutOfBoundsException ignore) {
            // Ignore if the coordinates are out of bounds
        }
    }

    /**
     * Unhighlights the cell at the specified coordinates.
     *
     * <p>This method unhighlights the cell at the specified coordinates in the Minesweeper game.
     * If the coordinates are out of bounds, the method ignores the operation.</p>
     *
     * @param x The x-coordinate of the cell to be unhighlighted.
     * @param y The y-coordinate of the cell to be unhighlighted.
     */
    private void unhighlightCell(int x, int y) {
        try {
            highlightedMines[x][y] = false;
        } catch (ArrayIndexOutOfBoundsException ignore) {
            // Ignore if the coordinates are out of bounds
        }
    }

    /**
     * Toggles the highlighting state of a cell at the specified coordinates.
     *
     * <p>This method toggles the highlighting state of the cell at the specified coordinates in the Minesweeper game.
     * If the cell has already been uncovered, highlighting is prevented.
     * The method swaps the x and y coordinates due to storage differences.
     * If the cell is already highlighted, it will be unhighlighted, and vice versa.</p>
     *
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     */
    public void toggleHighlightCell(int x, int y) {
        // Prevent highlighting if the cell has already been uncovered
        // X-Y coordinates are swapped because they are stored differently
        if (isUncovered(y, x)) {
            unhighlightCell(x, y);
            return;
        }
        if (isCellHighlighted(x, y)) {
            unhighlightCell(x, y);
        } else {
            highlightCell(x, y);
        }
    }

    /**
     * Checks if a cell at the specified coordinates has been uncovered.
     *
     * <p>This method verifies whether the cell at the specified coordinates has been uncovered in the Minesweeper game.
     * It returns {@code true} if the cell is uncovered, or {@code false} if the coordinates are out of bounds,
     * considering uncovered cells as already uncovered.</p>
     *
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     * @return {@code true} if the cell is uncovered or if the coordinates are out of bounds, {@code false} otherwise.
     */
    public boolean isUncovered(int x, int y) {
        try {
            return uncovered[x][y];
        } catch (ArrayIndexOutOfBoundsException ignore) {
            // Return true for out-of-bounds coordinates, considering uncovered cells as already uncovered
            return true;
        }
    }




    /**
     * Retrieves the character representation of the cell at the specified coordinates.
     *
     * <p>This method returns the character representation of the cell at the specified (x, y) coordinates.
     * If the coordinates are out of bounds, it returns the NOT_SET character from the CellType enum.</p>
     *
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     * @return The character representation of the cell at the specified coordinates or the NOT_SET character from the CellType enum
     *         if the coordinates are out of bounds.
     */
    public Cell getCell(int x, int y) {
        try {
            if (uncovered[x][y]){
                return matrix[x][y];
            }
            else {
                return new Cell(CellType.HIDDEN);
            }
        } catch (ArrayIndexOutOfBoundsException ignore) {
            // Return other value, because it will be ignored
            return new Cell(CellType.NOT_SET);
        }
    }

    /**
     * Retrieves the count of remaining mines in the Minesweeper grid.
     *
     * <p>This method iterates through the Minesweeper grid and counts the number of remaining mines.
     * It takes into account any flagged (highlighted) mines and subtracts them from the total count.</p>
     *
     * @return The count of remaining mines in the Minesweeper grid.
     */
    public int getRemainingMines() {
        int mines = 0;

        // Count mines in the matrix
        for (Cell[] line : matrix) {
            for (Cell cell : line) {
                if (cell.type == CellType.MINE) {
                    mines++;
                }
            }
        }

        // Subtract flagged (highlighted) mines
        for (boolean[] line : highlightedMines) {
            for (boolean bool : line) {
                if (bool) {
                    mines--;
                }
            }
        }

        return mines;
    }
}