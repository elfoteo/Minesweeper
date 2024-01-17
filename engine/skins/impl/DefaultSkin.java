package engine.skins.impl;

import engine.skins.Skin;
import engine.utils.CellType;

public class DefaultSkin implements Skin {
    @Override
    public char getChar(CellType cellType) {
        switch (cellType) {
            case NOT_SET -> {
                return '.';
            }
            case EMPTY -> {
                return ' ';
            }
            case NUMBER -> {
                return cellType.getNumberAsChar();
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
