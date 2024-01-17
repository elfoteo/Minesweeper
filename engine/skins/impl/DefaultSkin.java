package engine.skins.impl;

import engine.skins.Skin;
import engine.utils.Cell;
import engine.utils.CellType;

public class DefaultSkin implements Skin {
    @Override
    public char getChar(Cell cell) {
        switch (cell.type) {
            case NOT_SET -> {
                return '.';
            }
            case EMPTY -> {
                return ' ';
            }
            case NUMBER -> {
                return cell.getNumberAsChar();
            }
            case MINE -> {
                return '*';
            }
            case HIDDEN -> {
                return '#';
            }
        }
        return ' ';
    }
}
