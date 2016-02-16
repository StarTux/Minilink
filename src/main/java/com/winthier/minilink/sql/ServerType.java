package com.winthier.minilink.sql;

import com.avaje.ebean.annotation.EnumValue;

public enum ServerType {
    @EnumValue("game")
    GAME, // Game Server, running Minigames and Minilink
    @EnumValue("lobby")
    LOBBY, // Lobby Server, running Minilink
    // @EnumValue("default")
    // DEFAULT, // Default Lobby Server
    ;

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
