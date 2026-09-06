package io.github.derexxd.offlineTeleport.nbt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NbtManager {

    private final JavaPlugin plugin;

    public NbtManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

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

    public Location readLocation(OfflinePlayer player) {
        return player.getLocation();
    }

    public boolean writeLocation(OfflinePlayer player, Location location) {
        if (player.isOnline() || location == null || location.getWorld() == null) {
            return false;
        }

        Path file = playerDataFile(player.getUniqueId());
        if (file == null || !Files.isRegularFile(file)) {
            return false;
        }

        try {
            Nbt.FileData data = Nbt.readCompressed(file);
            applyLocation(data.root(), location);
            Nbt.writeCompressed(file, data);
            return true;
        } catch (IOException | RuntimeException exception) {
            plugin.getLogger().warning("Could not write player data for " + player.getUniqueId() + ": " + exception.getMessage());
            return false;
        }
    }

    private void applyLocation(Nbt.Compound root, Location location) {
        World world = location.getWorld();
        UUID worldId = world.getUID();
        root.put("Pos", new Nbt.ListTag(Nbt.DOUBLE, List.of(
                new Nbt.DoubleTag(location.getX()),
                new Nbt.DoubleTag(location.getY()),
                new Nbt.DoubleTag(location.getZ())
        )));
        root.put("Rotation", new Nbt.ListTag(Nbt.FLOAT, List.of(
                new Nbt.FloatTag(location.getYaw()),
                new Nbt.FloatTag(location.getPitch())
        )));
        root.put("Dimension", new Nbt.StringTag(world.getKey().toString()));
        root.put("WorldUUIDMost", new Nbt.LongTag(worldId.getMostSignificantBits()));
        root.put("WorldUUIDLeast", new Nbt.LongTag(worldId.getLeastSignificantBits()));
    }

    private Path playerDataFile(UUID uuid) {
        List<World> worlds = Bukkit.getWorlds();
        if (worlds.isEmpty()) {
            return null;
        }
        return worlds.get(0).getWorldFolder().toPath().resolve("playerdata").resolve(uuid + ".dat");
    }
}
