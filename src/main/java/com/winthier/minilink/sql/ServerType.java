package com.winthier.minilink.sql;

public enum ServerType {
    GAME, // Game Server, running Minigames and Minilink
    LOBBY, // Lobby Server, running Minilink
    // @EnumValue("default")
    // DEFAULT, // Default Lobby Server
    ;

    public final String key;

    ServerType() {
        this.key = name().toLowerCase();
    }

    public boolean isLobbyServer() {
        switch (this) {
        case LOBBY: /*case DEFAULT:*/ return true;
        default: return false;
        }
    }

    public boolean isGameServer() {
        return !isLobbyServer();
    }
}
