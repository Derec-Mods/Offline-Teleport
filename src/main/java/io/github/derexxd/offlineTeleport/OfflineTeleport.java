package io.github.derexxd.offlineTeleport;

import io.github.derexxd.offlineTeleport.command.OfflineTeleportCommand;
import io.github.derexxd.offlineTeleport.command.PlayerNameCache;
import io.github.derexxd.offlineTeleport.config.PluginMessages;
import io.github.derexxd.offlineTeleport.nbt.NbtManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class OfflineTeleport extends JavaPlugin {

    private PluginMessages messages;
    private NbtManager nbtManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new PluginMessages(this);
        nbtManager = new NbtManager(this);

        PlayerNameCache names = new PlayerNameCache();
        names.load();
        getServer().getPluginManager().registerEvents(names, this);

        PluginCommand offlinetp = getCommand("offlinetp");
        if (offlinetp != null) {
            OfflineTeleportCommand command = new OfflineTeleportCommand(this, names);
            offlinetp.setExecutor(command);
            offlinetp.setTabCompleter(command);
        }
    }

    public PluginMessages getMessages() {
        return messages;
    }

    public NbtManager getNbtManager() {
        return nbtManager;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
