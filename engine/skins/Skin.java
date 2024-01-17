package engine.skins;

import engine.utils.CellType;

/**
 * An interface defining the contract for different skins in the game.
 * Classes implementing this interface will provide specific characters for each CellType.
 */
public interface Skin {
    /**
     * Gets the character representation for a specific CellType in the Minesweeper game.
     *
     * @param cellType The type of the cell (e.g., EMPTY, MINE, NUMBER).
     * @return The character representation associated with the given CellType.
     */
    char getChar(CellType cellType);
}

