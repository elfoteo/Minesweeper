package engine.utils;

import engine.UIManager;

/**
 * Represents a single cell in a game grid.
 */
public class Cell {
    public CellType type;
    private boolean uncovered = false;
    private boolean flagged = false;
    // Additional property for NUMBER cells
    private int number;

    /**
     * Constructs a cell with the specified type.
     *
     * @param type The type of the cell.
     */
    public Cell(CellType type){
        this.type = type;
    }

    /**
     * Sets the number of adjacent mines for NUMBER cells.
     *
     * @param number The number of adjacent mines.
     * @throws IllegalStateException if called on a non-NUMBER cell.
     */
    public void setAdjacentMines(int number) {
        if (this.type == CellType.NUMBER) {
            this.number = number;
        } else {
            throw new IllegalStateException("Only NUMBER cells can have a number property.");
        }
    }

    /**
     * Gets the number of adjacent mines for NUMBER cells.
     *
     * @return The number of adjacent mines.
     * @throws IllegalStateException if called on a non-NUMBER cell.
     */
    public int getNumber() {
        if (this.type == CellType.NUMBER) {
            return number;
        } else {
            throw new IllegalStateException("Only NUMBER cells have a number property.");
        }
    }

    /**
     * Gets the number of adjacent mines as a character.
     *
     * @return The character representation of the number of adjacent mines.
     */
    public char getNumberAsChar(){
        return (char) (this.getNumber() + '0');
    }

    /**
     * Gets the character representation of the cell based on the selected skin.
     *
     * @return The character representation of the cell.
     */
    public char getChar(){
        return UIManager.selectedSkin.getChar(this);
    }

    /**
     * Gets the character representation for a specific cell type.
     *
     * @param type The type of the cell.
     * @return The character representation of the cell.
     */
    public static char getCharFor(CellType type){
        return (new Cell(type)).getChar();
    }

    /**
     * Checks if the cell is uncovered.
     *
     * @return true if the cell is uncovered, otherwise false.
     */
    public boolean isUncovered() {
        return uncovered;
    }

    /**
     * Sets whether the cell is uncovered.
     *
     * @param uncovered true to set the cell as uncovered, false otherwise.
     */
    public void setUncovered(boolean uncovered) {
        this.uncovered = uncovered;
    }

    /**
     * Checks if the cell is flagged.
     *
     * @return true if the cell is flagged, otherwise false.
     */
    public boolean isFlagged() {
        return flagged;
    }

    /**
     * Sets whether the cell is flagged.
     *
     * @param flagged true to set the cell as flagged, false otherwise.
     */
    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }
}
