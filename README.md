[![Support me on Ko-fi](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/donate/kofi-singular-alt_vector.svg)](https://ko-fi.com/derexxd) 

> If this project helped you, please consider leaving some stars [on the Github](https://github.com/Derec-Mods)! 

Essential admin utility for teleporting to offline players' last logout locations, teleporting offline players to other players, or moving offline players directly to specific coordinates. Perfect for server moderation, rescuing players stuck in glitched chunks, and server administration!

Admins have needed offline player teleportation for as long as multiplayer servers have existed! Most existing solutions are either bloated, abandoned, or rely on fragile NMS reflection hacks that break on every minor Minecraft update. 

## Features
- **Teleport to Offline Players**: Teleport directly to where an offline player logged out.
- **Move Offline Players**: Teleport an offline player to an online player, or vice versa, before they even rejoin.
- **Coordinate Teleportation**: Send offline players directly to custom X, Y, Z coordinates in any loaded world.
- **Safe & Native NBT Engine**: Fast, zero-dependency NBT parser and writer that safely updates `player.dat` without NMS reflection or heavy library overhead.
- **Seamless Live Support**: Automatically falls back to standard live teleportation if the player is currently online.
- **Smart Tab Completion**: Fast, cached offline player name suggestions for easy command usage.
- **Granular Permissions**: Restrict usage with fine-grained permission nodes (`offlinetp.offlinetp`, `offlinetp.to-player`, `offlinetp.player-to-player`).

## Commands & Permissions
- `/offlinetp <target>` (aliases: `/otp`, `/offlineteleport`) — Teleport to target player's logout location (`offlinetp.to-player` or `offlinetp.offlinetp`)
- `/offlinetp <target> <otherTarget>` — Teleport target to another player (`offlinetp.player-to-player` or `offlinetp.offlinetp`)
- `/offlinetp <target> <x> <y> <z>` — Teleport target to coordinates (`offlinetp.player-to-player` or `offlinetp.offlinetp`)

## ❤️ Credits:

Developed by DerexXD

This was created for use on my forever world, called "derex smp", and now I am sharing it with the world. The server is named after the COVID-era derex smp [sic], a Minecraft survival multiplayer server that ran from summer 2020 to early 2022, filled with lore, drama, and memories.