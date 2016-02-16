package com.winthier.minilink.util;

import com.winthier.minilink.sql.GameTable;
import com.winthier.minilink.sql.PlayerTable;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

/**
 * Static helper functions for Player conversions.
 */
public class Players {
    @SuppressWarnings("deprecation") // getPlayer(String) may be deprecated
    public static List<PlayerInfo> parsePlayers(String[] args, int beginIndex) {
        List<PlayerInfo> result = new ArrayList<>(args.length - beginIndex);
        for (int i = beginIndex; i < args.length; ++i) {
            Player player = Bukkit.getServer().getPlayer(args[i]);
            if (player == null) throw new CommandException("Player not found: " + args[i]);
            result.add(PlayerInfo.fromPlayer(player));
        }
        return result;
    }

    public static List<PlayerInfo> getPlayerInfos(List<Player> players) {
        List<PlayerInfo> result = new ArrayList<>(players.size());
        for (Player player : players) result.add(PlayerInfo.fromPlayer(player));
        return result;
    }

    public static List<PlayerInfo> getPlayerInfos(Player... players) {
        List<PlayerInfo> result = new ArrayList<>(players.length);
        for (Player player : players) result.add(PlayerInfo.fromPlayer(player));
        return result;
    }

    public static List<PlayerInfo> playersFromTable(GameTable gameTable) {
        List<PlayerInfo> result = new ArrayList<>(gameTable.getPlayers().size());
        for (PlayerTable player : gameTable.getPlayers()) result.add(PlayerInfo.fromInfo(player.getUuid(), player.getName()));
        return result;
    }

    @SuppressWarnings("deprecation")
    public static List<Player> getPlayers(List<String> playerNames) {
        List<Player> result = new ArrayList<>(playerNames.size());
        for (String playerName : playerNames) {
            Player player = Bukkit.getServer().getPlayerExact(playerName);
            if (player == null) throw new IllegalArgumentException("Player not found: " + playerNames);
            result.add(player);
        }
        return result;
    }
}
