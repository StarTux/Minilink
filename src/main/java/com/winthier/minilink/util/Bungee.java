package com.winthier.minilink.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Misc functions related to bungee.
 */
public class Bungee {
    public static void send(JavaPlugin plugin, Player player, String serverName) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("Connect");
            out.writeUTF(serverName);
        } catch (IOException ex) {
            // Impossible(?)
        }
        System.out.println("Sending " + player.getName() + " to " + serverName);
        player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
    }
}
