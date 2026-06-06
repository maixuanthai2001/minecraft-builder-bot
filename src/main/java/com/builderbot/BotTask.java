package com.builderbot;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BotTask {

    private final JavaPlugin plugin;
    private final ClaimManager claimManager;
    private BukkitTask schedulerTask;
    private boolean running = false;
    private int buildCount = 0;
    private static final Random RANDOM = new Random();

    // How often to auto-build (in ticks, 6000 = 5 minutes)
    private static final long BUILD_INTERVAL = 6000L;
    // Search radius for empty land
    private static final int SEARCH_RADIUS = 300;

    public BotTask(JavaPlugin plugin, ClaimManager claimManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
    }

    public void startScheduler() {
        if (running) return;
        running = true;
        schedulerTask = new BukkitRunnable() {
            @Override
            public void run() {
                buildNow();
            }
        }.runTaskTimer(plugin, 100L, BUILD_INTERVAL);
        plugin.getLogger().info("[BuilderBot] Scheduler started. Building every 5 minutes.");
    }

    public void stop() {
        running = false;
        if (schedulerTask != null) {
            schedulerTask.cancel();
            schedulerTask = null;
        }
    }

    public boolean isRunning() { return running; }
    public int getBuildCount() { return buildCount; }

    public void buildNow() {
        World world = Bukkit.getWorlds().get(0);
        // Pick random location to search
        int attempts = 0;
        while (attempts < 20) {
            int rx = RANDOM.nextInt(SEARCH_RADIUS * 2) - SEARCH_RADIUS;
            int rz = RANDOM.nextInt(SEARCH_RADIUS * 2) - SEARCH_RADIUS;
            int y = world.getHighestBlockYAt(rx, rz) + 1;

            // Pick random structure
            int type = RANDOM.nextInt(5);
            int sizeX, sizeZ;
            switch (type) {
                case 0: sizeX = 9; sizeZ = 9; break;   // house
                case 1: sizeX = 5; sizeZ = 5; break;   // tower
                case 2: sizeX = 13; sizeZ = 13; break; // castle
                case 3: sizeX = 7; sizeZ = 7; break;   // pyramid
                default: sizeX = 6; sizeZ = 6; break;  // well+garden
            }

            if (claimManager.isClear(world, rx, rz, sizeX, sizeZ)) {
                claimManager.addBuiltZone(rx, rz, sizeX, sizeZ);
                buildStructure(world, rx, y, rz, type);
                buildCount++;
                Bukkit.broadcastMessage(ChatColor.GOLD + "[BuilderBot] " + ChatColor.YELLOW +
                    "Da xay xong cong trinh moi tai (" + rx + ", " + y + ", " + rz + ")! " +
                    ChatColor.AQUA + "Tong: " + buildCount + " cong trinh.");
                return;
            }
            attempts++;
        }
        plugin.getLogger().info("[BuilderBot] Khong tim duoc vi tri trong sau " + attempts + " lan thu.");
    }

    private void buildStructure(World world, int x, int y, int z, int type) {
        switch (type) {
            case 0: buildHouse(world, x, y, z); break;
            case 1: buildTower(world, x, y, z); break;
            case 2: buildCastle(world, x, y, z); break;
            case 3: buildPyramid(world, x, y, z); break;
            default: buildWellAndGarden(world, x, y, z); break;
        }
    }

    // ---- HOUSE ----
    private void buildHouse(World world, int x, int y, int z) {
        Material wall = pickRandom(Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS, Material.STONE_BRICKS);
        Material roof = pickRandom(Material.DARK_OAK_SLAB, Material.SPRUCE_SLAB, Material.STONE_SLAB);
        Material glass = Material.GLASS_PANE;

        // Floor
        fillBlocks(world, x, y - 1, z, x + 8, y - 1, z + 8, Material.COBBLESTONE);
        // Walls
        for (int dx = 0; dx <= 8; dx++) {
            for (int dz = 0; dz <= 8; dz++) {
                if (dx == 0 || dx == 8 || dz == 0 || dz == 8) {
                    for (int dy = 0; dy <= 4; dy++) {
                        world.getBlockAt(x + dx, y + dy, z + dz).setType(wall);
                    }
                }
            }
        }
        // Windows
        world.getBlockAt(x + 2, y + 2, z).setType(glass);
        world.getBlockAt(x + 6, y + 2, z).setType(glass);
        world.getBlockAt(x + 2, y + 2, z + 8).setType(glass);
        world.getBlockAt(x + 6, y + 2, z + 8).setType(glass);
        world.getBlockAt(x, y + 2, z + 2).setType(glass);
        world.getBlockAt(x, y + 2, z + 6).setType(glass);
        world.getBlockAt(x + 8, y + 2, z + 2).setType(glass);
        world.getBlockAt(x + 8, y + 2, z + 6).setType(glass);
        // Door
        world.getBlockAt(x + 4, y, z).setType(Material.AIR);
        world.getBlockAt(x + 4, y + 1, z).setType(Material.AIR);
        world.getBlockAt(x + 4, y, z).setType(Material.OAK_DOOR);
        // Roof (flat slab)
        fillBlocks(world, x, y + 5, z, x + 8, y + 5, z + 8, roof);
        // Interior: torches
        world.getBlockAt(x + 2, y + 1, z + 2).setType(Material.TORCH);
        world.getBlockAt(x + 6, y + 1, z + 6).setType(Material.TORCH);
        // Crafting table + chest inside
        world.getBlockAt(x + 2, y, z + 4).setType(Material.CRAFTING_TABLE);
        world.getBlockAt(x + 6, y, z + 4).setType(Material.CHEST);
    }

    // ---- TOWER ----
    private void buildTower(World world, int x, int y, int z) {
        Material mat = pickRandom(Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.COBBLESTONE, Material.DEEPSLATE_BRICKS);
        int height = 10 + RANDOM.nextInt(6);
        // Base platform
        fillBlocks(world, x - 1, y - 1, z - 1, x + 5, y - 1, z + 5, Material.STONE_BRICKS);
        // Tower walls
        for (int dy = 0; dy < height; dy++) {
            for (int dx = 0; dx <= 4; dx++) {
                for (int dz = 0; dz <= 4; dz++) {
                    if (dx == 0 || dx == 4 || dz == 0 || dz == 4) {
                        world.getBlockAt(x + dx, y + dy, z + dz).setType(mat);
                    }
                }
            }
        }
        // Battlements on top
        for (int dx = 0; dx <= 4; dx += 2) {
            world.getBlockAt(x + dx, y + height, z).setType(mat);
            world.getBlockAt(x + dx, y + height, z + 4).setType(mat);
        }
        for (int dz = 0; dz <= 4; dz += 2) {
            world.getBlockAt(x, y + height, z + dz).setType(mat);
            world.getBlockAt(x + 4, y + height, z + dz).setType(mat);
        }
        // Door
        world.getBlockAt(x + 2, y, z).setType(Material.AIR);
        world.getBlockAt(x + 2, y + 1, z).setType(Material.AIR);
        // Lantern on top
        world.getBlockAt(x + 2, y + height, z + 2).setType(Material.LANTERN);
        // Ladder inside
        for (int dy = 0; dy < height; dy++) {
            world.getBlockAt(x + 1, y + dy, z + 1).setType(Material.LADDER);
        }
    }

    // ---- CASTLE ----
    private void buildCastle(World world, int x, int y, int z) {
        Material wall = Material.STONE_BRICKS;
        Material floor = Material.STONE;
        // Outer walls
        for (int dx = 0; dx <= 12; dx++) {
            for (int dz = 0; dz <= 12; dz++) {
                if (dx == 0 || dx == 12 || dz == 0 || dz == 12) {
                    for (int dy = 0; dy <= 5; dy++) {
                        world.getBlockAt(x + dx, y + dy, z + dz).setType(wall);
                    }
                } else {
                    world.getBlockAt(x + dx, y - 1, z + dz).setType(floor);
                }
            }
        }
        // Gate
        world.getBlockAt(x + 6, y, z).setType(Material.AIR);
        world.getBlockAt(x + 6, y + 1, z).setType(Material.AIR);
        world.getBlockAt(x + 6, y + 2, z).setType(Material.AIR);
        // Corner towers (mini)
        int[][] corners = {{x, z}, {x + 10, z}, {x, z + 10}, {x + 10, z + 10}};
        for (int[] c : corners) {
            for (int dy = 0; dy <= 7; dy++) {
                for (int dx2 = 0; dx2 <= 2; dx2++) {
                    for (int dz2 = 0; dz2 <= 2; dz2++) {
                        if (dx2 == 0 || dx2 == 2 || dz2 == 0 || dz2 == 2) {
                            world.getBlockAt(c[0] + dx2, y + dy, c[1] + dz2).setType(wall);
                        }
                    }
                }
            }
            world.getBlockAt(c[0] + 1, y + 8, c[1] + 1).setType(Material.LANTERN);
        }
        // Courtyard torch
        world.getBlockAt(x + 6, y, z + 6).setType(Material.CAMPFIRE);
    }

    // ---- PYRAMID ----
    private void buildPyramid(World world, int x, int y, int z) {
        Material mat = pickRandom(Material.SANDSTONE, Material.SMOOTH_SANDSTONE, Material.CUT_SANDSTONE);
        int layers = 4;
        for (int layer = 0; layer < layers; layer++) {
            int offset = layer;
            int size = 7 - (layer * 2);
            if (size < 1) size = 1;
            fillBlocks(world, x + offset, y + layer, z + offset,
                       x + offset + size - 1, y + layer, z + offset + size - 1, mat);
        }
        // Top decoration
        world.getBlockAt(x + 3, y + layers, z + 3).setType(Material.GOLD_BLOCK);
    }

    // ---- WELL + GARDEN ----
    private void buildWellAndGarden(World world, int x, int y, int z) {
        // Well
        for (int dx = 0; dx <= 2; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                if (dx == 0 || dx == 2 || dz == 0 || dz == 2) {
                    world.getBlockAt(x + dx, y, z + dz).setType(Material.STONE_BRICKS);
                    world.getBlockAt(x + dx, y + 1, z + dz).setType(Material.STONE_BRICKS);
                } else {
                    world.getBlockAt(x + dx, y, z + dz).setType(Material.WATER);
                }
            }
        }
        // Well roof
        world.getBlockAt(x + 1, y + 2, z + 1).setType(Material.OAK_FENCE);
        world.getBlockAt(x, y + 3, z).setType(Material.OAK_SLAB);
        world.getBlockAt(x + 2, y + 3, z).setType(Material.OAK_SLAB);
        world.getBlockAt(x, y + 3, z + 2).setType(Material.OAK_SLAB);
        world.getBlockAt(x + 2, y + 3, z + 2).setType(Material.OAK_SLAB);
        // Garden around well
        Material[] flowers = {Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID,
                              Material.ALLIUM, Material.CORNFLOWER, Material.OXEYE_DAISY};
        for (int dx = -2; dx <= 4; dx++) {
            for (int dz = -2; dz <= 4; dz++) {
                if (Math.abs(dx) > 1 || Math.abs(dz) > 1) {
                    world.getBlockAt(x + dx, y - 1, z + dz).setType(Material.GRASS_BLOCK);
                    if (RANDOM.nextInt(3) == 0) {
                        world.getBlockAt(x + dx, y, z + dz).setType(flowers[RANDOM.nextInt(flowers.length)]);
                    }
                }
            }
        }
    }

    // ---- HELPERS ----
    private void fillBlocks(World world, int x1, int y1, int z1, int x2, int y2, int z2, Material mat) {
        for (int x = x1; x <= x2; x++)
            for (int y = y1; y <= y2; y++)
                for (int z = z1; z <= z2; z++)
                    world.getBlockAt(x, y, z).setType(mat);
    }

    @SafeVarargs
    private <T> T pickRandom(T... options) {
        return options[RANDOM.nextInt(options.length)];
    }
}
