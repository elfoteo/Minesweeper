package engine.utils;

public enum CellType {
    NOT_SET,
    EMPTY,
    MINE,
    NUMBER;

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
        switch (this){
            case NOT_SET -> {
                return '.';
            }
            case EMPTY -> {
                return ' ';
            }
            case NUMBER -> {
                return this.getNumberAsChar();
            }
            case MINE -> {
                return '*';
            }
        }
        return ' ';
    }
}
