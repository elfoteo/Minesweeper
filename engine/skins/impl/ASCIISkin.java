package engine.skins.impl;

import engine.skins.ISkin;
import engine.utils.Cell;

import java.io.Serializable;

public class ASCIISkin implements ISkin, Serializable {
    @Override
    public char getChar(Cell cell) {
        switch (cell.type) {
            case NOT_SET:
                return '.';
            case NUMBER:
                return cell.getNumberAsChar();
            case MINE:
                return '#';
            case HIDDEN:
                return '@';
            default:
                return ' ';
        }
    }

    @Override
    public String getSkinName() {
        return "ASCII Art";
    }
}
