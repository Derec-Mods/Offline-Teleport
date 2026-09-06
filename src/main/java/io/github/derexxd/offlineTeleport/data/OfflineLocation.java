package io.github.derexxd.offlineTeleport.data;

import org.bukkit.Location;

public final class OfflineLocation {

    public enum Status {
        FOUND,
        MISSING,
        WORLD_UNAVAILABLE
    }

    private final Status status;
    private final Location location;
    private final String worldName;

    private OfflineLocation(Status status, Location location, String worldName) {
        this.status = status;
        this.location = location;
        this.worldName = worldName;
    }

    public static OfflineLocation found(Location location) {
        return new OfflineLocation(Status.FOUND, location, location.getWorld().getName());
    }

    public static OfflineLocation missing() {
        return new OfflineLocation(Status.MISSING, null, null);
    }

    public static OfflineLocation worldUnavailable(String worldName) {
        return new OfflineLocation(Status.WORLD_UNAVAILABLE, null, worldName);
    }

    public Status status() {
        return status;
    }

    public Location location() {
        return location;
    }

    public String worldName() {
        return worldName;
    }
}
