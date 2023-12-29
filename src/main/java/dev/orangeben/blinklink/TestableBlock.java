package dev.orangeben.blinklink;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

public class TestableBlock {

    public enum TestType {
        MATERIAL, PASSABLE;
    }

    private int dx, dy, dz;
    private TestType type;
    private Material mat;

    public static final int rots[][] = {{1, 0, 0, 1}, {0, -1, 1, 0}, {-1, 0, 0, -1}, {0, 1, -1, 0}};

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

    public boolean test(Location l) {
        return test(l, 0, null, false);
    }

    public boolean test(Location l, Player p) {
        return test(l, 0, p, false);
    }

    public boolean test(Location l, int dir) {
        return test(l, dir, null, false);
    }

    public boolean test(Location l, int dir, Player p) {
        return test(l, dir, p, false);
    }

    @SuppressWarnings("deprecation")
    public boolean test(Location l, int dir, Player p, boolean suppressParticle) {
        int rx = rots[dir][0] * dx + rots[dir][1] * dz;
        int rz = rots[dir][2] * dx + rots[dir][3] * dz;
        Location tgt = l.clone().add(rx, dy, rz);
        String matstr = (type == TestType.MATERIAL) ? mat.toString() : "#passable"; 
        if(
            (type == TestType.MATERIAL && tgt.getBlock().getType() == mat) ||
            (type == TestType.PASSABLE && tgt.getBlock().isPassable())
        ) {
            if(BlinkLink.config.getBoolean(ConfigKeys.TEST_BROADCAST)) {
                Bukkit.broadcastMessage("Found " + matstr + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ".");
            }
            if(p != null && BlinkLink.config.getBoolean(ConfigKeys.TEST_MESSAGE)) {
                p.sendMessage("Found " + matstr + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ".");
            }
            if(p != null && BlinkLink.config.getBoolean(ConfigKeys.TEST_PARTICLE) && !suppressParticle) {
                DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 255, 0), 1.0F);
                p.spawnParticle(Particle.REDSTONE, tgt.clone().add(0.5, 0.5, 0.5), 10, dustOptions);
            }
            return true;
        }
        if(BlinkLink.config.getBoolean(ConfigKeys.TEST_BROADCAST)) {
            Bukkit.broadcastMessage("Looking for " + matstr + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ", found " + tgt.getBlock().getType());
        }
        if(p != null && BlinkLink.config.getBoolean(ConfigKeys.TEST_MESSAGE)) {
            p.sendMessage("Looking for " + matstr + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ", found " + tgt.getBlock().getType());
        }
        if(p != null && BlinkLink.config.getBoolean(ConfigKeys.TEST_PARTICLE) && !suppressParticle) {
            DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 0, 0), 1.0F);
            p.spawnParticle(Particle.REDSTONE, tgt.clone().add(0.5, 0.5, 0.5), 10, dustOptions);
            p.spawnParticle(Particle.EXPLOSION_NORMAL, tgt.clone().add(0.5, 0.5, 0.5), 1);
        }
        return false;
    }
}