package engine.utils;

/**
 * Enumerates the types of cells in a game grid.
 */
public enum CellType {
    NOT_SET, // Cell type not determined
    EMPTY,   // Empty cell
    MINE,    // Mine cell
    NUMBER,  // Cell with a number indicating adjacent mines
    HIDDEN;  // Hidden cell
}
