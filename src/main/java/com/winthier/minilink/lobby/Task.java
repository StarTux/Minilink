package com.winthier.minilink.lobby;

import com.winthier.minilink.message.Message;
import java.util.UUID;

public abstract class Task {
    protected final LobbyServer lobby;
    protected final UUID uuid;

    public Task(LobbyServer lobby, UUID uuid) {
        this.lobby = lobby;
        this.uuid = uuid;
    }

    public Task(LobbyServer lobby) {
        this(lobby, UUID.randomUUID());
    }

    public void stop() {
        lobby.removeTask(uuid);
    }

    public abstract void tick();
    public abstract void handleMessage(Message message);
}
