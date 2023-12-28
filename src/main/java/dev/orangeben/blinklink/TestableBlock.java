package dev.orangeben.blinklink;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

public class TestableBlock {
    int dx, dy, dz;
    Material mat;

    public static final int rots[][] = {{1, 0, 0, 1}, {0, -1, 1, 0}, {-1, 0, 0, -1}, {0, 1, -1, 0}};

    TestableBlock(int dx, int dy, int dz, Material mat) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.mat = mat;
    }

    public boolean test(Location l, int dir) {
        return test(l, dir, null);
    }
    
    @SuppressWarnings("deprecation")
    public boolean test(Location l, int dir, Player p) {
        int rx = rots[dir][0] * dx + rots[dir][1] * dz;
        int rz = rots[dir][2] * dx + rots[dir][3] * dz;
        Location tgt = l.clone().add(rx, dy, rz);
        if(tgt.getBlock().getType() == mat) {
            if(BlinkLink.config.getBoolean(ConfigKeys.TEST_BROADCAST)) {
                Bukkit.broadcastMessage("Found " + mat + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ".");
            }
            if(p != null && BlinkLink.config.getBoolean(ConfigKeys.TEST_MESSAGE)) {
                p.sendMessage("Found " + mat + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ".");
            }
            if(p != null && BlinkLink.config.getBoolean(ConfigKeys.TEST_PARTICLE)) {
                DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 255, 0), 1.0F);
                p.spawnParticle(Particle.REDSTONE, tgt.clone().add(0.5, 0.5, 0.5), 10, dustOptions);
            }
            return true;
        }
        if(BlinkLink.config.getBoolean(ConfigKeys.TEST_BROADCAST)) {
            Bukkit.broadcastMessage("Looking for " + mat + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ", found " + tgt.getBlock().getType());
        }
        if(p != null && BlinkLink.config.getBoolean(ConfigKeys.TEST_MESSAGE)) {
            p.sendMessage("Looking for " + mat + " at " + tgt.getBlockX() + ", " + tgt.getBlockY() + ", " + tgt.getBlockZ() + " " + tgt.getWorld().getName() + ", found " + tgt.getBlock().getType());
        }
        if(p != null && BlinkLink.config.getBoolean(ConfigKeys.TEST_PARTICLE)) {
            DustOptions dustOptions = new DustOptions(Color.fromRGB(255, 0, 0), 1.0F);
            p.spawnParticle(Particle.REDSTONE, tgt.clone().add(0.5, 0.5, 0.5), 10, dustOptions);
        }
        return false;
    }
}