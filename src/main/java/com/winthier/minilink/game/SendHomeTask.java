package com.winthier.minilink.game;

import com.winthier.minilink.MinilinkPlugin;
import com.winthier.minilink.sql.ServerTable;
import com.winthier.minilink.util.Bungee;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SendHomeTask extends BukkitRunnable {
    private final MinilinkPlugin plugin;
    private final Player player;
    private List<String> servers = null;
    private int count = 0;

    public SendHomeTask(MinilinkPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override public void run() {
        if (!player.isValid()) {
            stop();
            return;
        }
        int i = count++;
        if (i == 0) {
            servers = plugin.database.getReturnServers(player);
            if (!servers.isEmpty()) {
                Bungee.send(plugin, player, servers.get(0));
            }
        } else if (i < servers.size()) {
            Bungee.send(plugin, player, servers.get(i));
        } else if (i > servers.size() + 4) {
            player.kickPlayer("Game Over");
            stop();
        }
    }

    public void start() {
        runTaskTimer(plugin, 0, 60);
    }

    public void stop() {
        try {
            cancel();
        } catch (IllegalStateException ise) {}
    }

    public static void sendHome(MinilinkPlugin plugin, Player player) {
        SendHomeTask task = new SendHomeTask(plugin, player);
        task.start();
    }
}
