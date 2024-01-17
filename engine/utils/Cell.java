package engine.utils;

import engine.skins.Skin;
import engine.skins.impl.DefaultSkin;

public class Cell {
    public CellType type;
    private static Skin skin = new DefaultSkin();
    // Additional property for NUMBER cells
    private int number;

    public Cell(CellType type){
        this.type = type;
    }

    // Constructor for NUMBER cells
    public void setAdjacentMines(int number) {
        if (this.type == CellType.NUMBER) {
            this.number = number;
        } else {
            throw new IllegalStateException("Only NUMBER cells can have a number property.");
        }
    }

    // Getter for the number property
    public int getNumber() {
        if (this.type == CellType.NUMBER) {
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

    public static char getCharFor(CellType type){
        return (new Cell(type)).getChar();
    }
}
