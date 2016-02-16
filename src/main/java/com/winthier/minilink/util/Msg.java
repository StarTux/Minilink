package com.winthier.minilink.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONValue;

/**
 * Misc. messaging utility functions
 */
public class Msg {
    public static String format(String msg, Object... args) {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        if (args.length > 0) msg = String.format(msg, args);
        return msg;
    }

    public static void send(CommandSender sender, String msg, Object... args) {
        sender.sendMessage(format(msg, args));
    }

    public static void sendRaw(Player player, Object json) {
        String js;
        try {
            js = JSONValue.toJSONString(json);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "minecraft:tellraw " + player.getName() + " " + js);
    }
}
