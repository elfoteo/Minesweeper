package engine.skins.impl;

import engine.skins.ISkin;
import engine.utils.Cell;

import java.io.Serializable;

public class DiamondSkin implements ISkin, Serializable {
    @Override
    public char getChar(Cell cell) {
        switch (cell.type) {
            case NOT_SET:
                return '.';
            case NUMBER:
                return cell.getNumberAsChar();
            case MINE:
                return '\u25C6';
            case HIDDEN:
                return '\u25C7';
            default:
                return ' ';
        }
    }

    @Override
    public String getSkinName() {
        return "Diamond";
    }
}
