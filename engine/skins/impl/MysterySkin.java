package engine.skins.impl;

import engine.skins.Skin;
import engine.utils.Cell;

import java.io.Serializable;

public class MysterySkin implements Skin, Serializable {
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
                return '?';
            }
        }
        return ' ';
    }

    @Override
    public String getSkinName() {
        return "Mystery";
    }
}
