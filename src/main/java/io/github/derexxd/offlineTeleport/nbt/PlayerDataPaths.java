package io.github.derexxd.offlineTeleport.nbt;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class PlayerDataPaths {

    public enum Layout {
        LEGACY,
        MODERN
    }

    private static final Path LEGACY_DIR = Path.of("playerdata");
    private static final Path MODERN_DIR = Path.of("players", "data");

    private PlayerDataPaths() {
    }

    public static Layout layout() {
        Version version = parseMinecraftVersion();
        if (version != null && version.isModernPlayerStorage()) {
            return Layout.MODERN;
        }
        if (anyRootHas(MODERN_DIR) && !anyRootHas(LEGACY_DIR)) {
            return Layout.MODERN;
        }
        return Layout.LEGACY;
    }

    public static String minecraftVersion() {
        Version version = parseMinecraftVersion();
        return version != null ? version.raw : "unknown";
    }

    public static Path find(UUID uuid) {
        for (Path candidate : candidates(uuid)) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    public static List<Path> candidates(UUID uuid) {
        String fileName = uuid + ".dat";
        List<Path> relativeDirs = layout() == Layout.MODERN
                ? List.of(MODERN_DIR, LEGACY_DIR)
                : List.of(LEGACY_DIR, MODERN_DIR);

        Set<Path> files = new LinkedHashSet<>();
        for (Path relative : relativeDirs) {
            for (Path root : levelRoots()) {
                files.add(root.resolve(relative).resolve(fileName));
            }
        }
        return new ArrayList<>(files);
    }

    private static boolean anyRootHas(Path relativeDir) {
        for (Path root : levelRoots()) {
            if (Files.isDirectory(root.resolve(relativeDir))) {
                return true;
            }
        }
        return false;
    }

    private static List<Path> levelRoots() {
        Set<Path> roots = new LinkedHashSet<>();
        for (World world : Bukkit.getWorlds()) {
            Path folder = world.getWorldFolder().toPath().toAbsolutePath().normalize();
            roots.add(folder);
            Path levelRoot = findLevelRoot(folder);
            if (levelRoot != null) {
                roots.add(levelRoot);
            }
        }
        roots.add(Bukkit.getWorldContainer().toPath().toAbsolutePath().normalize());
        return new ArrayList<>(roots);
    }

    private static Path findLevelRoot(Path folder) {
        Path current = folder;
        for (int i = 0; i < 6 && current != null; i++) {
            if (Files.isDirectory(current.resolve("players"))
                    || Files.isDirectory(current.resolve("playerdata"))
                    || Files.isRegularFile(current.resolve("level.dat"))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    private static Version parseMinecraftVersion() {
        Version fromMinecraft = parse(Bukkit.getMinecraftVersion());
        if (fromMinecraft != null) {
            return fromMinecraft;
        }
        return parse(Bukkit.getBukkitVersion());
    }

    private static Version parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        String numeric = raw.split("[^0-9.]")[0];
        String[] parts = numeric.split("\\.");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return null;
        }
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return new Version(raw, major, minor);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record Version(String raw, int major, int minor) {
        boolean isModernPlayerStorage() {
            return major > 26 || (major == 26 && minor >= 1);
        }
    }
}
