package com.winthier.minilink.lobby;

import com.winthier.minilink.util.Msg;
import com.winthier.minilink.util.Players;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyMiniMeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        final Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage("Player expected");
            return true;
        }
        if (args.length == 0) { return true; }
        String firstArg = args[0].toLowerCase();
        if (false) {
        } else if ("join".equals(firstArg) && args.length == 2) {
            final UUID uuid;
            try {
                uuid = UUID.fromString(args[1]);
            } catch (IllegalArgumentException iae) {
                return true;
            }
            LobbyServer.getInstance().joinGame(uuid, Players.getPlayerInfos(player));
        } else if ("spec".equals(firstArg) && args.length == 2) {
            final UUID uuid;
            try {
                uuid = UUID.fromString(args[1]);
            } catch (IllegalArgumentException iae) {
                return true;
            }
            LobbyServer.getInstance().spectateGame(uuid, Players.getPlayerInfos(player));
        }
        return true;
    }
}
