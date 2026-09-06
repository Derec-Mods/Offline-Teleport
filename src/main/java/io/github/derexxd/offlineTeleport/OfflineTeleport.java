package io.github.derexxd.offlineTeleport;

import io.github.derexxd.offlineTeleport.config.PluginMessages;
import io.github.derexxd.offlineTeleport.data.OfflineLocationService;
import org.bukkit.plugin.java.JavaPlugin;

public final class OfflineTeleport extends JavaPlugin {

    private PluginMessages messages;
    private OfflineLocationService locations;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new PluginMessages(this);
        locations = new OfflineLocationService(this);
    }

    public PluginMessages getMessages() {
        return messages;
    }

    public OfflineLocationService getLocations() {
        return locations;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
