package engine.options;

public class OptionsInstance {
    private String username;
    private boolean grayOutNearbyCells;

    public OptionsInstance(String username, boolean grayOutNearbyCells) {
        if (username.length() < 10){
            this.username = username;
        }
        else{
            this.username = "null";
        }
        this.grayOutNearbyCells = grayOutNearbyCells;
    }

    public boolean isGrayOutNearbyCells() {
        return grayOutNearbyCells;
    }

    public void setGrayOutNearbyCells(boolean grayOutNearbyCells) {
        this.grayOutNearbyCells = grayOutNearbyCells;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
