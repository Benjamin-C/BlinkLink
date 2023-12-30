package dev.orangeben.blinklink;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class BLCommand implements CommandExecutor {

    private Plugin plugin;

    public BLCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(Messages.CONFIG_RELOADED);
        plugin.reloadConfig();
        BLPlugin.config = plugin.getConfig();
        return true;
    }
}