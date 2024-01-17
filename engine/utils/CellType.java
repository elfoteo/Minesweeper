package engine.utils;

import engine.skins.Skin;
import engine.skins.impl.DefaultSkin;

public enum CellType {
    NOT_SET,
    EMPTY,
    MINE,
    NUMBER,
    HIDDEN;

    private static Skin skin = new DefaultSkin();
    // Additional property for NUMBER cells
    private int number;

    // Constructor for NUMBER cells
    public void setNumber(int number) {
        if (this == NUMBER) {
            this.number = number;
        } else {
            throw new IllegalStateException("Only NUMBER cells can have a number property.");
        }
    }

    // Getter for the number property
    public int getNumber() {
        if (this == NUMBER) {
            return number;
        } else {
            throw new IllegalStateException("Only NUMBER cells have a number property.");
        }
    }

    public char getNumberAsChar(){
        return (char) (this.getNumber() + '0');
    }

    public char getChar(){
        return skin.getChar(this);
    }
}
