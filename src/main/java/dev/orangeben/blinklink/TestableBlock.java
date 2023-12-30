package dev.orangeben.blinklink;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

/**
 * A block that you can test what it is. Supports using an offset in distance and direction from a center location of a structure. 
 */
public class TestableBlock {

    /** Types of tests a TestableBlock can do */
    public enum TestType {
        /** Look for a material */
        MATERIAL,
        /** Look for a block where {@link org.bukkit.Block#isPassable() isPassable()} is true} */
        PASSABLE;
    }

    /** The X offset from the center location */ 
    private int dx;
    /** The Y offset from the center location */ 
    private int dy;
    /** The Z offset from the center location */ 
    private int dz;
    /** The type of test to apply */
    private TestType type;
    /** The material to test for if applicable, otherwise null */
    private Material mat;

    /** Rotation matrix for each direction */
    public static final int rots[][] = {{1, 0, 0, 1}, {0, -1, 1, 0}, {-1, 0, 0, -1}, {0, 1, -1, 0}};

    /** Internal use only, must set type/material separately */
    private TestableBlock(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    TestableBlock(int dx, int dy, int dz, Material mat) {
        this(dx, dy, dz);
        this.type = TestType.MATERIAL;
        this.mat = mat;
    }

    TestableBlock(int dx, int dy, int dz, TestType type) {
        this(dx, dy, dz);
        this.type = type;
        this.mat = null;
    }

    /**
     * Checks if this test passes relative to a given location
     * @param l the location to test from
     * @return  if the test was passed
     */
    public boolean test(Location l) {
        return test(l, 0, null, false);
    }

    /**
     * Checks if this test passes relative to a given location, possibly informing the player
     * @param l the Location to test from
     * @param p the Player to inform
     * @return  if the test was passed
     */
    public boolean test(Location l, Player p) {
        return test(l, 0, p, false);
    }

    /**
     * Checks if this test passes relative to a given location, rotating the offsets by dir*90 degrees
     * @param l   the Location to test from
     * @param dir the direction index to rotate the offset
     * @return  if the test was passed
     */
    public boolean test(Location l, int dir) {
        return test(l, dir, null, false);
    }

    /**
     * Checks if this test passes relative to a given location, rotating the offsets by dir*90 degrees, possibly informing the player
     * @param l   the Location to test from
     * @param dir the direction index to rotate the offset
     * @param p the Player to inform
     * @return  if the test was passed
     */
    public boolean test(Location l, int dir, Player p) {
        return test(l, dir, p, false);
    }

    /**
     * Checks if this test passes relative to a given location, rotating the offsets by dir*90 degrees, possibly informing the player and optionally hiding particles
     * @param l   the Location to test from
     * @param dir the direction index to rotate the offset
     * @param p   the Player to inform
     * @param suppressParticle Don't show particles if true even if config says to
     * @return  if the test was passed
     */
    @SuppressWarnings("deprecation")
    public boolean test(Location l, int dir, Player p, boolean suppressParticle) {
        // Calculate the offsets after rotation
        int rx = rots[dir][0] * dx + rots[dir][1] * dz;
        int rz = rots[dir][2] * dx + rots[dir][3] * dz;
        // Create a new Location where we are actually testing
        Location tgt = l.clone().add(rx, dy, rz);
        // What text to show the player if we are doing that
        String matstr = (type == TestType.MATERIAL) ? mat.toString() : "#passable";
        // Test if the block matches the criteria
        if(
            (type == TestType.MATERIAL && tgt.getBlock().getType() == mat) ||
            (type == TestType.PASSABLE && tgt.getBlock().isPassable())
        ) {
            // If the block matches ...
            // Broadcast messages to all players
            if(BLPlugin.config.getBoolean(ConfigKeys.TEST_BROADCAST)) {
                Bukkit.broadcastMessage("Found " + matstr + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ".");
            }
            // Send messages to the player initiating the test
            if(p != null && BLPlugin.config.getBoolean(ConfigKeys.TEST_MESSAGE)) {
                p.sendMessage("Found " + matstr + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ".");
            }
            // Create particles
            if(p != null && BLPlugin.config.getBoolean(ConfigKeys.TEST_PARTICLE) && !suppressParticle) {
                DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 255, 0), 1.0F);
                p.spawnParticle(Particle.REDSTONE, tgt.clone().add(0.5, 0.5, 0.5), 10, dustOptions);
            }
            // Say the block matched
            return true;
        }
        // If the block didn't match ...
        // Broadcast messages to all players
        if(BLPlugin.config.getBoolean(ConfigKeys.TEST_BROADCAST)) {
            Bukkit.broadcastMessage("Looking for " + matstr + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ", found " + tgt.getBlock().getType());
        }
        // Send messages to the player initiating the test
        if(p != null && BLPlugin.config.getBoolean(ConfigKeys.TEST_MESSAGE)) {
            p.sendMessage("Looking for " + matstr + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ", found " + tgt.getBlock().getType());
        }
        // Create particles
        if(p != null && BLPlugin.config.getBoolean(ConfigKeys.TEST_PARTICLE) && !suppressParticle) {
            DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 0, 0), 1.0F);
            p.spawnParticle(Particle.REDSTONE, tgt.clone().add(0.5, 0.5, 0.5), 10, dustOptions);
            p.spawnParticle(Particle.EXPLOSION_NORMAL, tgt.clone().add(0.5, 0.5, 0.5), 1);
        }
        // Say the block didn't match
        return false;
    }
}