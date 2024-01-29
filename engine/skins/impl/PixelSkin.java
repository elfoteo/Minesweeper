package engine.skins.impl;

import engine.skins.ISkin;
import engine.utils.Cell;

import java.io.Serializable;

public class PixelSkin implements ISkin, Serializable {
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
                return '\u25A0';
            }
            case HIDDEN -> {
                return '\u25A1';
            }
        }
        return ' ';
    }

    @Override
    public String getSkinName() {
        return "Pixel";
    }
}
