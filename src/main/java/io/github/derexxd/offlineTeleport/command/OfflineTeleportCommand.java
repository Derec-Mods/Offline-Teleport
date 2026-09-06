package io.github.derexxd.offlineTeleport.command;

import io.github.derexxd.offlineTeleport.OfflineTeleport;
import io.github.derexxd.offlineTeleport.config.PluginMessages;
import io.github.derexxd.offlineTeleport.nbt.NbtManager;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class OfflineTeleportCommand implements CommandExecutor {

    public static final String PERMISSION_FULL = "offlinetp.offlinetp";
    public static final String PERMISSION_TO_PLAYER = "offlinetp.to-player";
    public static final String PERMISSION_PLAYER_TO_PLAYER = "offlinetp.player-to-player";

    private final OfflineTeleport plugin;

    public OfflineTeleportCommand(OfflineTeleport plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!canUseAny(sender)) {
                messages().send(sender, "no-permission");
                return true;
            }
            messages().send(sender, "usage");
            return true;
        }
        if (args.length == 1) {
            teleportTo(sender, args[0]);
            return true;
        }
        if (args.length == 2) {
            teleportPlayerToPlayer(sender, args[0], args[1]);
            return true;
        }
        messages().send(sender, "usage");
        return true;
    }

    private void teleportTo(CommandSender sender, String targetName) {
        if (!sender.hasPermission(PERMISSION_FULL) && !sender.hasPermission(PERMISSION_TO_PLAYER)) {
            messages().send(sender, "no-permission");
            return;
        }
        if (!(sender instanceof Player player)) {
            messages().send(sender, "player-only");
            return;
        }

        OfflinePlayer target = requirePlayer(sender, targetName);
        if (target == null) {
            return;
        }

        Location location = requireLocation(sender, target, targetName);
        if (location == null) {
            return;
        }

        player.teleport(location);
        messages().send(sender, "teleported-to", "player", nameOf(target, targetName));
    }

    private void teleportPlayerToPlayer(CommandSender sender, String targetName, String otherName) {
        if (!sender.hasPermission(PERMISSION_FULL) && !sender.hasPermission(PERMISSION_PLAYER_TO_PLAYER)) {
            messages().send(sender, "no-permission");
            return;
        }

        OfflinePlayer target = requirePlayer(sender, targetName);
        if (target == null) {
            return;
        }
        OfflinePlayer other = requirePlayer(sender, otherName);
        if (other == null) {
            return;
        }

        Location location = requireLocation(sender, other, otherName);
        if (location == null) {
            return;
        }

        if (target.isOnline()) {
            Player online = target.getPlayer();
            if (online == null || !online.teleport(location)) {
                messages().send(sender, "location-unavailable", "player", nameOf(target, targetName));
                return;
            }
        } else if (!nbt().writeLocation(target, location)) {
            messages().send(sender, "location-unavailable", "player", nameOf(target, targetName));
            return;
        }

        messages().send(sender, "teleported-player",
                "target", nameOf(target, targetName),
                "other", nameOf(other, otherName));
    }

    private OfflinePlayer requirePlayer(CommandSender sender, String name) {
        return nbt().findByName(name).orElseGet(() -> {
            messages().send(sender, "player-not-found", "player", name);
            return null;
        });
    }

    private Location requireLocation(CommandSender sender, OfflinePlayer player, String fallbackName) {
        Location location = nbt().readLocation(player);
        if (location == null) {
            if (!player.hasPlayedBefore() && !player.isOnline()) {
                messages().send(sender, "never-played", "player", nameOf(player, fallbackName));
            } else {
                messages().send(sender, "location-unavailable", "player", nameOf(player, fallbackName));
            }
            return null;
        }
        if (location.getWorld() == null) {
            messages().send(sender, "world-unavailable", "world", "unknown");
            return null;
        }
        return location;
    }

    private boolean canUseAny(CommandSender sender) {
        return sender.hasPermission(PERMISSION_FULL)
                || sender.hasPermission(PERMISSION_TO_PLAYER)
                || sender.hasPermission(PERMISSION_PLAYER_TO_PLAYER);
    }

    private String nameOf(OfflinePlayer player, String fallback) {
        String name = player.getName();
        return name != null ? name : fallback;
    }

    private PluginMessages messages() {
        return plugin.getMessages();
    }

    private NbtManager nbt() {
        return plugin.getNbtManager();
    }
}
