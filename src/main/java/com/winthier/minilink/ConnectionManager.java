package com.winthier.minilink;

import com.winthier.connect.Connect;
import com.winthier.connect.bukkit.event.ConnectMessageEvent;
import com.winthier.minilink.message.Message;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * A blackbox class that sends and receives Messages to and from other
 * servers.
 */
public class ConnectionManager implements Listener {
    public final MinilinkPlugin plugin;

    public ConnectionManager(MinilinkPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onConnectMessage(ConnectMessageEvent event) {
        if (!event.getMessage().getChannel().equals("Minilink")) return;
        try {
            byte[] data = ((String)event.getMessage().getPayload()).getBytes();
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            Object obj = in.readObject();
            if (!(obj instanceof Message)) throw new RuntimeException("Received packet of unexpected type " + obj.getClass().getName());
            Message message = (Message)obj;
            message.setSourceServer(event.getMessage().getFrom());
            plugin.receiveMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(Message message) {
        message.saveOptions();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(message);
            byte[] data = baos.toByteArray();
            Connect.getInstance().broadcast("Minilink", new String(data));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String getServerName() {
        return Connect.getInstance().getName();
    }
}
