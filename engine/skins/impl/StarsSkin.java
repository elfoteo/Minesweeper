package engine.skins.impl;

import engine.skins.ISkin;
import engine.utils.Cell;

import java.io.Serializable;

public class StarsSkin implements ISkin, Serializable {
    @Override
    public char getChar(Cell cell) {
        if (cell.isFlagged()){
            return '\u25CA';
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
            case MINE -> {
                return '\u29EB';
            }
            case HIDDEN -> {
                return '\u262A';
            }
        }
        return ' ';
    }

    @Override
    public String getSkinName() {
        return "Stars";
    }
}
