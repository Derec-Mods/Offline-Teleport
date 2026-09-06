package io.github.derexxd.offlineTeleport.command;

import io.github.derexxd.offlineTeleport.nbt.NbtManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class OfflineTeleportCommand implements CommandExecutor, TabCompleter {

    public static final String PERMISSION_FULL = "offlinetp.offlinetp";
    public static final String PERMISSION_TO_PLAYER = "offlinetp.to-player";
    public static final String PERMISSION_PLAYER_TO_PLAYER = "offlinetp.player-to-player";

    private final NbtManager nbt;
    private final PlayerNameCache names;

    public OfflineTeleportCommand(NbtManager nbt, PlayerNameCache names) {
        this.nbt = nbt;
        this.names = names;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!canUseAny(sender)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                return true;
            }
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " <target> [otherTarget]");
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
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " <target> [otherTarget]");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!canUseAny(sender)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return names.suggestOffline(args[0]);
        }
        if (args.length == 2) {
            return names.suggestAll(args[1]);
        }
        return Collections.emptyList();
    }

    private void teleportTo(CommandSender sender, String targetName) {
        if (!sender.hasPermission(PERMISSION_FULL) && !sender.hasPermission(PERMISSION_TO_PLAYER)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can teleport to another player.");
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
        sender.sendMessage(ChatColor.GREEN + "Teleported to " + ChatColor.WHITE + nameOf(target, targetName) + ChatColor.GREEN + ".");
    }

    private void teleportPlayerToPlayer(CommandSender sender, String targetName, String otherName) {
        if (!sender.hasPermission(PERMISSION_FULL) && !sender.hasPermission(PERMISSION_PLAYER_TO_PLAYER)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
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
                sender.sendMessage(ChatColor.RED + "Could not read the last location for " + ChatColor.WHITE + nameOf(target, targetName) + ChatColor.RED + ".");
                return;
            }
        } else if (!nbt.writeLocation(target, location)) {
            sender.sendMessage(ChatColor.RED + "Could not update the last location for " + ChatColor.WHITE + nameOf(target, targetName) + ChatColor.RED + ".");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "Teleported " + ChatColor.WHITE + nameOf(target, targetName)
                + ChatColor.GREEN + " to " + ChatColor.WHITE + nameOf(other, otherName) + ChatColor.GREEN + ".");
    }

    private OfflinePlayer requirePlayer(CommandSender sender, String name) {
        return nbt.findByName(name).orElseGet(() -> {
            sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + name + ChatColor.RED + " was not found.");
            return null;
        });
    }

    private Location requireLocation(CommandSender sender, OfflinePlayer player, String fallbackName) {
        Location location = nbt.readLocation(player);
        if (location == null) {
            if (!player.hasPlayedBefore() && !player.isOnline()) {
                sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + nameOf(player, fallbackName) + ChatColor.RED + " has never joined this server.");
            } else {
                sender.sendMessage(ChatColor.RED + "Could not read the last location for " + ChatColor.WHITE + nameOf(player, fallbackName) + ChatColor.RED + ".");
            }
            return null;
        }
        if (location.getWorld() == null) {
            sender.sendMessage(ChatColor.RED + "That player's world is not loaded.");
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
}
