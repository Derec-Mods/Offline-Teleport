package io.github.derexxd.offlineTeleport.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerNameCache implements Listener {

    private final Set<String> names = ConcurrentHashMap.newKeySet();

    public void load() {
        names.clear();
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            String name = player.getName();
            if (name != null && !name.isEmpty()) {
                names.add(name);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        names.add(event.getPlayer().getName());
    }

    public List<String> suggest(String prefix, boolean offlineOnly) {
        String needle = prefix == null ? "" : prefix;
        List<String> matches = new ArrayList<>();
        for (String name : names) {
            if (!name.regionMatches(true, 0, needle, 0, needle.length())) {
                continue;
            }
            if (offlineOnly && Bukkit.getPlayerExact(name) != null) {
                continue;
            }
            matches.add(name);
        }
        Collections.sort(matches, String.CASE_INSENSITIVE_ORDER);
        return matches;
    }
}
