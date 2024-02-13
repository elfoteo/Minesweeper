package engine.skins.impl;

import engine.skins.ISkin;
import engine.utils.Cell;

import java.io.Serializable;

public class ASCIISkin implements ISkin, Serializable {
    @Override
    public char getChar(Cell cell) {
        return switch (cell.type) {
            case NOT_SET -> '.';
            case NUMBER -> cell.getNumberAsChar();
            case MINE -> '#';
            case HIDDEN -> '@';
            default -> ' ';
        };
    }

    @Override
    public String getSkinName() {
        return "ASCII Art";
    }
}
