package engine;

import engine.utils.Tuple;

import java.util.Random;

public class Minesweeper {
    private final char[][] matrix;
    private final boolean[][] uncovered;
    private final boolean[][] highlightedMines;
    private static final Random random = new Random();
    private int uncoverCount = 0;

    public Minesweeper(int width, int height, int mines) {
        matrix = new char[width][height];
        uncovered = new boolean[width][height];
        highlightedMines = new boolean[width][height];
        // Initialize the matrix and uncovered arrays
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                matrix[x][y] = ' ';
                uncovered[x][y] = false;
            }
        }

        // Place mines randomly on the matrix

        for (int i = 0; i < mines; i++) {
            int randomX, randomY;

            do {
                randomX = random.nextInt(0, width);
                randomY = random.nextInt(0, height);

            } while (matrix[randomX][randomY] == '*');

            matrix[randomX][randomY] = '*';
        }

        // Place numbers
        placeNumbers();
    }

    private void placeNumbers(){
        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix[0].length; y++) {
                int adjacentMines = getNumbersOfMines(x, y);
                if (adjacentMines != 0 && !isMine(x, y)){
                    matrix[x][y] = (char)(adjacentMines + '0');
                }
            }
        }
    }

    public int getFieldWidth(){
        return matrix.length;
    }

    public int getFieldHeight(){
        return matrix[0].length;
    }


    public String getFieldAsString() {
        StringBuilder res = new StringBuilder();


        for (int x = 0; x < matrix.length; x++) {
            for (int y = 0; y < matrix[0].length; y++) {
                if (isUncovered(x, y)) {
                    res.append(matrix[x][y]);
                } else {
                    res.append("#");
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

    public Tuple<Character, Tuple<Integer, Boolean>> uncover(int x, int y) {
        if (isCellHighlighted(x, y)){
            return new Tuple<>(null, new Tuple<>(0, false));
        }
        boolean wasUncovered;
        char cellValue = 'X';
        int score = 0;
        boolean gameEnded = true;

        try {
            // The first time the player uncovers a tile must always be a safe tile
            if (uncoverCount == 0){
                matrix[y][x] = ' ';
                placeNumbers();
            }
            uncoverCount++;
            wasUncovered = uncovered[y][x];
            uncovered[y][x] = true;
            cellValue = matrix[y][x];
            // If the cell wasn't already uncovered
            if (!wasUncovered){
                score++;

                if (cellValue == ' ') {
                    // Check and uncover adjacent cells recursively
                    score += uncoverAdjacent(x - 1, y);
                    score += uncoverAdjacent(x + 1, y);
                    score += uncoverAdjacent(x, y - 1);
                    score += uncoverAdjacent(x, y + 1);
                }
            }

            for (x = 0; x < matrix.length; x++) {
                for (y = 0; y < matrix[0].length; y++) {
                    if (!isUncovered(x, y) && matrix[x][y] != '*'){
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

        return new Tuple<>(cellValue, new Tuple<>(score, gameEnded));
    }

    private int uncoverAdjacent(int x, int y) {
        try {
            if (!uncovered[y][x] && matrix[y][x] != '*') {
                return uncover(x, y).second().first();
            }
        } catch (ArrayIndexOutOfBoundsException ignore) {}
        return 0;
    }

    private boolean isMine(int x, int y){
        try{
            return matrix[x][y] == '*';
        }
        catch (ArrayIndexOutOfBoundsException ignore){
            // Ignore if the coordinates are out of bounds
        }
        return false;
    }

    private int getNumbersOfMines(int x, int y) {
        int num = 0;

        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                if ((i != x || j != y) && isMine(i, j)) {
                    num++;
                }
            }
        }

        return num;
    }

    public boolean isCellHighlighted(int x, int y){
        try{
            return highlightedMines[x][y];
        }catch (ArrayIndexOutOfBoundsException ignore){
            // Ignore if the coordinates are out of bounds
        }
        return false;
    }

    private void highlightCell(int x, int y){
        try{
            highlightedMines[x][y] = true;
        }catch (ArrayIndexOutOfBoundsException ignore){
            // Ignore if the coordinates are out of bounds
        }
    }

    private void unhighlightCell(int x, int y) {
        try {
            highlightedMines[x][y] = false;
        } catch (ArrayIndexOutOfBoundsException ignore) {
            // Ignore if the coordinates are out of bounds
        }
    }

    public void toggleHighlightCell(int x, int y){
        // Prevent highlighting if the cell has already been uncovered
        if (isUncovered(y, x)){
            unhighlightCell(x, y);
            return;
        }
        if (isCellHighlighted(x, y)){
            unhighlightCell(x, y);
        }
        else {
            highlightCell(x, y);
        }
    }

    public boolean isUncovered(int x, int y) {
        try{
            return uncovered[x][y];
        }
        catch (ArrayIndexOutOfBoundsException ignore){

        }
        return true;
    }

    public char getCell(int x, int y){
        try{
            return matrix[x][y];
        }
        catch (ArrayIndexOutOfBoundsException ignore){

        }
        return '.';
    }

    public int getRemainingMines(){
        int mines = 0;
        for (char[] line : matrix){
            for (char character : line){
                if (character == '*'){
                    mines++;
                }
            }
        }
        for (boolean[] line : highlightedMines){
            for (boolean bool : line){
                if (bool){
                    mines--;
                }
            }
        }
        return mines;
    }
}