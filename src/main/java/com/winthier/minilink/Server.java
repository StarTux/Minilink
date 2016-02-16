package com.winthier.minilink;

import com.winthier.minilink.message.Message;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Superclass of both sides that can send and receive messages:
 * The minigames server and the lobby server(s).
 */
public abstract class Server implements CommandExecutor {
    public final MinilinkPlugin plugin;

    public Server(MinilinkPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void enable();
    public abstract void disable();

    /**
     * Handle an incoming message.
     */
    public abstract void handleMessage(Message message);

    public abstract boolean command(CommandSender sender, Command command, String label, String args[]);

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        try {
            if (args.length == 0) {
                return false;
            } else if (args.length == 1 && "DumpGameConfigs".equalsIgnoreCase(args[0])) {
                plugin.database.dumpGameConfigs();
                sender.sendMessage("Dumped game configs");
            } else if (args.length == 1 && "LoadGameConfigs".equalsIgnoreCase(args[0])) {
                plugin.database.loadGameConfigs();
                sender.sendMessage("Loaded game configs");
            } else if (args.length == 1 && "Reboot".equalsIgnoreCase(args[0])) {
                plugin.broadcastMessage(Message.Type.REBOOT.create());
                sender.sendMessage("Notified all game servers to reboot ASAP");
            } else {
                return command(sender, command, label, args);
            }
        } catch (CommandException ce) {
            sender.sendMessage("" + ChatColor.RED + ce.getMessage());
            return true;
        }
        return true;
    }
}
