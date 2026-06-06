package com.builderbot;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ClaimManager {

    private final JavaPlugin plugin;
    // List of claimed zones: each int[]{x1,z1,x2,z2} in world coords
    private final List<int[]> claimedZones = new ArrayList<>();
    // Bot-built zones to avoid overlap
    private final List<int[]> builtZones = new ArrayList<>();

    // Minimum distance between any two structures
    private static final int MIN_SEPARATION = 30;
    // Buffer around player last known position
    private static final int PLAYER_BUFFER = 50;

    public ClaimManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void addBuiltZone(int x, int z, int sizeX, int sizeZ) {
        builtZones.add(new int[]{x, z, x + sizeX, z + sizeZ});
    }

    public boolean isClear(World world, int x, int z, int sizeX, int sizeZ) {
        int x2 = x + sizeX;
        int z2 = z + sizeZ;

        // Check against online players' positions
        for (Player p : world.getPlayers()) {
            Location loc = p.getLocation();
            if (overlapsWithBuffer(x, z, x2, z2,
                    (int) loc.getX(), (int) loc.getZ(),
                    (int) loc.getX(), (int) loc.getZ(),
                    PLAYER_BUFFER)) {
                return false;
            }
        }

        // Check against already built bot zones
        for (int[] zone : builtZones) {
            if (overlapsWithBuffer(x, z, x2, z2, zone[0], zone[1], zone[2], zone[3], MIN_SEPARATION)) {
                return false;
            }
        }

        // Check claimed zones
        for (int[] zone : claimedZones) {
            if (overlapsWithBuffer(x, z, x2, z2, zone[0], zone[1], zone[2], zone[3], MIN_SEPARATION)) {
                return false;
            }
        }

        return true;
    }

    private boolean overlapsWithBuffer(int ax1, int az1, int ax2, int az2,
                                        int bx1, int bz1, int bx2, int bz2,
                                        int buffer) {
        return ax1 - buffer < bx2 + buffer &&
               ax2 + buffer > bx1 - buffer &&
               az1 - buffer < bz2 + buffer &&
               az2 + buffer > bz1 - buffer;
    }
}
