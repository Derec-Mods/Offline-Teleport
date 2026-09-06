package io.github.derexxd.offlineTeleport.config;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginMessages {

    private final JavaPlugin plugin;

    public PluginMessages(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String format(String path, String... replacements) {
        String text = plugin.getConfig().getString("messages." + path, path);
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        text = text.replace("{prefix}", prefix);
        if (replacements != null) {
            for (int i = 0; i + 1 < replacements.length; i += 2) {
                text = text.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public void send(CommandSender sender, String path, String... replacements) {
        sender.sendMessage(format(path, replacements));
    }
}
