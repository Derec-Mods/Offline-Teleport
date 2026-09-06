package io.github.derexxd.offlineTeleport.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public final class OfflineLocationService {

    public Optional<OfflinePlayer> findByName(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            String playerName = player.getName();
            if (playerName != null && playerName.equalsIgnoreCase(name)) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    public OfflineLocation readLocation(OfflinePlayer player) {
        Location location = player.getLocation();
        if (location == null) {
            return OfflineLocation.missing();
        }
        if (location.getWorld() == null) {
            return OfflineLocation.worldUnavailable("unknown");
        }
        return OfflineLocation.found(location);
    }
}
