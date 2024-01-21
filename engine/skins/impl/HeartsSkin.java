package engine.skins.impl;

import engine.skins.ISkin;
import engine.utils.Cell;

import java.io.Serializable;

public class HeartsSkin implements ISkin, Serializable {
    @Override
    public char getChar(Cell cell) {
        if (cell.isFlagged()){
            return '\u2661';
        }
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
            case MINE, HIDDEN -> {
                return '\u2665';
            }
        }
        return ' ';
    }

    @Override
    public String getSkinName() {
        return "Hearts";
    }
}
