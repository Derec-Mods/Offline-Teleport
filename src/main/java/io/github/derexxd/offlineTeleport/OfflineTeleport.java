package io.github.derexxd.offlineTeleport;

import io.github.derexxd.offlineTeleport.config.PluginMessages;
import org.bukkit.plugin.java.JavaPlugin;

public final class OfflineTeleport extends JavaPlugin {

    private PluginMessages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new PluginMessages(this);
    }

    public PluginMessages getMessages() {
        return messages;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
