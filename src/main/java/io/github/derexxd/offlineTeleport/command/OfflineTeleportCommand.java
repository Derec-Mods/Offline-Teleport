package io.github.derexxd.offlineTeleport.command;

import io.github.derexxd.offlineTeleport.nbt.NbtManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
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
            sendUsage(sender, label);
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
        if (args.length == 4) {
            teleportPlayerToCoords(sender, args[0], args[1], args[2], args[3]);
            return true;
        }
        sendUsage(sender, label);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!canUseAny(sender)) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return names.suggest(args[0], true);
        }
        if (args.length == 2) {
            return names.suggest(args[1], false);
        }
        return Collections.emptyList();
    }

    private void teleportTo(CommandSender sender, String targetName) {
        if (!sender.hasPermission(PERMISSION_FULL) && !sender.hasPermission(PERMISSION_TO_PLAYER)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }
        if (!(sender instanceof Player player)) {
            nbt.debug("teleport-to aborted: sender is not a player (" + sender.getName() + ")");
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

        if (!player.teleport(location)) {
            nbt.debug("teleport-to failed: " + sender.getName() + " could not teleport to "
                    + nameOf(target, targetName) + " at " + describeLocation(location));
            sender.sendMessage(ChatColor.RED + "Could not teleport to " + ChatColor.WHITE + nameOf(target, targetName) + ChatColor.RED + ".");
            return;
        }
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

        if (!movePlayer(sender, target, targetName, location)) {
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "Teleported " + ChatColor.WHITE + nameOf(target, targetName)
                + ChatColor.GREEN + " to " + ChatColor.WHITE + nameOf(other, otherName) + ChatColor.GREEN + ".");
    }

    private void teleportPlayerToCoords(CommandSender sender, String targetName, String xArg, String yArg, String zArg) {
        if (!sender.hasPermission(PERMISSION_FULL) && !sender.hasPermission(PERMISSION_PLAYER_TO_PLAYER)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
            return;
        }

        OfflinePlayer target = requirePlayer(sender, targetName);
        if (target == null) {
            return;
        }

        Double x = parseCoordinate(sender, xArg);
        Double y = parseCoordinate(sender, yArg);
        Double z = parseCoordinate(sender, zArg);
        if (x == null || y == null || z == null) {
            return;
        }

        World world;
        if (sender instanceof Player player) {
            world = player.getWorld();
        } else {
            Location currentWorld = target.getLocation();
            world = currentWorld != null ? currentWorld.getWorld() : null;
        }
        if (world == null) {
            Location currentWorld = target.getLocation();
            nbt.debug("coords aborted for " + nameOf(target, targetName) + ": no world (sender="
                    + sender.getName() + ", targetLocation=" + (currentWorld == null ? "null" : describeLocation(currentWorld)) + ")");
            sender.sendMessage(ChatColor.RED + "Could not determine a world for those coordinates.");
            return;
        }

        Location current = target.getLocation();
        float yaw = current != null ? current.getYaw() : 0F;
        float pitch = current != null ? current.getPitch() : 0F;
        Location location = new Location(world, x, y, z, yaw, pitch);

        if (!movePlayer(sender, target, targetName, location)) {
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "Teleported " + ChatColor.WHITE + nameOf(target, targetName)
                + ChatColor.GREEN + " to " + ChatColor.WHITE + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + ChatColor.GREEN + ".");
    }

    private boolean movePlayer(CommandSender sender, OfflinePlayer target, String targetName, Location location) {
        if (target.isOnline()) {
            Player online = target.getPlayer();
            if (online == null) {
                nbt.debug("move failed for " + nameOf(target, targetName) + ": isOnline=true but getPlayer() is null");
                sender.sendMessage(ChatColor.RED + "Could not teleport " + ChatColor.WHITE + nameOf(target, targetName) + ChatColor.RED + ".");
                return false;
            }
            if (!online.teleport(location)) {
                nbt.debug("move failed for " + nameOf(target, targetName) + ": live teleport returned false to " + describeLocation(location));
                sender.sendMessage(ChatColor.RED + "Could not teleport " + ChatColor.WHITE + nameOf(target, targetName) + ChatColor.RED + ".");
                return false;
            }
            return true;
        }
        if (!nbt.writeLocation(target, location)) {
            sender.sendMessage(ChatColor.RED + "Could not update the last location for " + ChatColor.WHITE + nameOf(target, targetName) + ChatColor.RED + ".");
            return false;
        }
        return true;
    }

    private Double parseCoordinate(CommandSender sender, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            nbt.debug("invalid coordinate '" + value + "' from " + sender.getName());
            sender.sendMessage(ChatColor.RED + "Invalid coordinate: " + ChatColor.WHITE + value);
            return null;
        }
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " <target> [otherTarget]");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " <target> <x> <y> <z>");
    }

    private OfflinePlayer requirePlayer(CommandSender sender, String name) {
        return nbt.findByName(name).orElseGet(() -> {
            nbt.debug("player not found: '" + name + "' is not in Bukkit.getOfflinePlayers()");
            sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + name + ChatColor.RED + " was not found.");
            return null;
        });
    }

    private Location requireLocation(CommandSender sender, OfflinePlayer player, String fallbackName) {
        Location location = player.getLocation();
        if (location == null) {
            nbt.debug("no location for " + nameOf(player, fallbackName) + " uuid=" + player.getUniqueId()
                    + " hasPlayedBefore=" + player.hasPlayedBefore() + " isOnline=" + player.isOnline());
            if (!player.hasPlayedBefore() && !player.isOnline()) {
                sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + nameOf(player, fallbackName) + ChatColor.RED + " has never joined this server.");
            } else {
                sender.sendMessage(ChatColor.RED + "Could not read the last location for " + ChatColor.WHITE + nameOf(player, fallbackName) + ChatColor.RED + ".");
            }
            return null;
        }
        if (location.getWorld() == null) {
            nbt.debug("world missing for " + nameOf(player, fallbackName) + " at "
                    + location.getX() + "," + location.getY() + "," + location.getZ()
                    + " (Paper returned a location with a null world)");
            sender.sendMessage(ChatColor.RED + "That player's world is not loaded.");
            return null;
        }
        return location;
    }

    private String describeLocation(Location location) {
        World world = location.getWorld();
        String worldName = world != null ? world.getName() : "null-world";
        return worldName + " " + location.getX() + "," + location.getY() + "," + location.getZ();
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
