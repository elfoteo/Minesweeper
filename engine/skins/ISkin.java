package engine.skins;

import engine.utils.Cell;

import java.io.Serializable;

/**
 * An interface defining the constraints for different skins in the game.
 * Classes implementing this interface will need to implement all methods in it.
 * Extending Serializable because later on the skin will need to be written to a file and serialized.
 */
public interface ISkin extends Serializable {
    /**
     * Gets the character representation for a specific CellType in the Minesweeper game.
     * The skin implementation can decide the character to output based on the given one.
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

