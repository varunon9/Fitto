package me.varunon9.fitto;

class Singleton {

    private static Singleton singleton;
    private String playerName;


    private Singleton() { }

    static synchronized Singleton getInstance() {
        if (singleton == null) {
            singleton = new Singleton();
        }
        return singleton;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
