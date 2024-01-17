package engine.skins;

import engine.utils.Cell;
/**
 * An interface defining the contract for different skins in the game.
 * Classes implementing this interface will provide specific characters for each CellType.
 */
public interface Skin {
    /**
     * Gets the character representation for a specific CellType in the Minesweeper game.
     *
     * @param cell The cell.
     * @return The character representation associated with the given CellType.
     */
    char getChar(Cell cell);
}

