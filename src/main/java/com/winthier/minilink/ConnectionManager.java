package com.winthier.minilink;

import com.winthier.connect.Connect;
import com.winthier.connect.bukkit.event.ConnectMessageEvent;
import com.winthier.minilink.message.Message;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
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
            byte[] data = Base64.getDecoder().decode((String)event.getMessage().getPayload());
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            Object obj = in.readObject();
            in.close();
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
            out.close();
            String string = Base64.getEncoder().encodeToString(baos.toByteArray());
            Connect.getInstance().broadcast("Minilink", string);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String getServerName() {
        return Connect.getInstance().getName();
    }
}
