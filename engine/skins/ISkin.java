package engine.skins;

import engine.utils.Cell;

import java.io.Serializable;

/**
 * An interface defining the contract for different skins in the game.
 * Classes implementing this interface will provide specific characters for each CellType.
 * Extending Serializable because later on the skin will need to be written to a file.
 */
public interface ISkin extends Serializable {
    /**
     * Gets the character representation for a specific CellType in the Minesweeper game.
     *
     * @param cell The cell.
     * @return The character representation associated with the given CellType.
     */
    char getChar(Cell cell);

    /**
     * Gets the name of the skin.
     *
     * @return The name of the skin.
     */
    String getSkinName();
}

