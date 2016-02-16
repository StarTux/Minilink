package com.winthier.minilink.lobby;

import com.winthier.minilink.message.Message;
import com.winthier.minilink.sql.GameTable;
import com.winthier.minilink.util.Configure;
import com.winthier.minilink.util.PlayerInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class JoinOrCreateTask extends Task {
    final String gameKey;
    final ConfigurationSection gameConfig;
    final List<PlayerInfo> players;
    final boolean doCreate;
    // State
    Iterator<GameTable> gameIter;
    int idleTime;

    public JoinOrCreateTask(LobbyServer lobby, String gameKey, ConfigurationSection gameConfig, List<PlayerInfo> players, boolean doCreate, UUID messageUuid) {
        super(lobby, messageUuid);
        this.gameKey = gameKey;
        this.gameConfig = gameConfig;
        this.players = players;
        this.doCreate = doCreate;
    }

    public void start() {
        List<GameTable> games = lobby.plugin.database.getOpenGamesByKey(gameKey);
        this.gameIter = games.iterator();
        tryJoinNext();
    }

    private void tryJoinNext() {
        if (gameIter.hasNext()) {
            idleTime = 0;
            GameTable gameTable = gameIter.next();
            lobby.joinGame(gameTable.getUuid(), players, this.uuid);
        } else {
            stop();
            if (doCreate) {
                lobby.createGame(gameKey, gameConfig, players, this.uuid);
            }
        }
    }

    @Override
    public void tick() {
        int idleTime = this.idleTime++;
        if (idleTime >= 2) {
            tryJoinNext();
        }
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.getType()) {
        case JOIN_GAME_REPLY:
            if (Configure.loadResult(message.getOptions())) {
                stop();
            } else {
                tryJoinNext();
            }
            break;
        default:
            lobby.plugin.getLogger().warning(getClass().getSimpleName() + " received unexpected message " + message.getType().name() + ": " + message.getOptionsString());
        }
    }
}
