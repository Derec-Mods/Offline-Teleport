package io.github.derexxd.offlineTeleport;

import io.github.derexxd.offlineTeleport.command.OfflineTeleportCommand;
import io.github.derexxd.offlineTeleport.command.PlayerNameCache;
import io.github.derexxd.offlineTeleport.nbt.NbtManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class OfflineTeleport extends JavaPlugin {

    @Override
    public void onEnable() {
        NbtManager nbtManager = new NbtManager(this);

        PlayerNameCache names = new PlayerNameCache();
        names.load();
        getServer().getPluginManager().registerEvents(names, this);

        PluginCommand offlinetp = getCommand("offlinetp");
        if (offlinetp != null) {
            OfflineTeleportCommand command = new OfflineTeleportCommand(nbtManager, names);
            offlinetp.setExecutor(command);
            offlinetp.setTabCompleter(command);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
