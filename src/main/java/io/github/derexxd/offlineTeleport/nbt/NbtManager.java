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
import java.util.logging.Level;

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

    public boolean writeLocation(OfflinePlayer player, Location location) {
        String who = describe(player);
        if (player.isOnline()) {
            debug("write skipped for " + who + ": player is online, so player.dat must not be edited");
            return false;
        }
        if (location == null) {
            debug("write skipped for " + who + ": destination location is null");
            return false;
        }
        if (location.getWorld() == null) {
            debug("write skipped for " + who + ": destination world is null");
            return false;
        }

        List<World> worlds = Bukkit.getWorlds();
        if (worlds.isEmpty()) {
            debug("write skipped for " + who + ": server has no loaded worlds");
            return false;
        }
        Path file = PlayerDataPaths.find(player.getUniqueId());
        if (file == null) {
            debug("write skipped for " + who + ": player.dat not found (layout=" + PlayerDataPaths.layout()
                    + ", mc=" + PlayerDataPaths.minecraftVersion() + ")");
            for (Path candidate : PlayerDataPaths.candidates(player.getUniqueId())) {
                debug("  checked " + candidate.toAbsolutePath() + " exists=" + Files.isRegularFile(candidate));
            }
            return false;
        }

        try {
            Nbt.FileData data = Nbt.readCompressed(file);
            Nbt.Compound root = data.root();
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
            Nbt.writeCompressed(file, data);
            return true;
        } catch (IOException | RuntimeException exception) {
            debug("write failed for " + who + " at " + file.toAbsolutePath() + ": " + exception);
            plugin.getLogger().log(Level.WARNING, "OfflineTP NBT write exception", exception);
            return false;
        }
    }

    public void debug(String reason) {
        plugin.getLogger().warning("[OfflineTP] " + reason);
    }

    private String describe(OfflinePlayer player) {
        String name = player.getName();
        return (name != null ? name : "unknown") + " (" + player.getUniqueId() + ")";
    }
}
