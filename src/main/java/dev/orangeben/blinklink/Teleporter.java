package dev.orangeben.blinklink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Teleporter {
    
    private Location from;
    private Location to;

    public Teleporter(Location from, Location to) {
        this.from = from.clone();
        this.to = to.clone().add(0.5, 0, 0.5);
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    // Check if this teleporter is fully operation
    public boolean isOperational() {
        return isSenderFunctional(from) && isReceiverFunctional(to);
    }

    @Override
    public String toString() {
        return "Teleporter (" + from.getBlockX() + ", " + from.getBlockY() + ", " + from.getBlockZ() + ")";
    }

    @Override
    public boolean equals(Object thatObj) {
        if(thatObj != null && thatObj instanceof Teleporter) {
            Teleporter that = (Teleporter) thatObj;
            return this.from.getWorld().equals(that.from.getWorld()) && 
                this.from.getBlockX() == that.from.getBlockX() && 
                this.from.getBlockY() == that.from.getBlockY() && 
                this.from.getBlockZ() == that.from.getBlockZ() && 
                this.to.getBlockX() == that.to.getBlockX() &&
                this.to.getBlockY() == that.to.getBlockY() &&
                this.to.getBlockZ() == that.to.getBlockZ();
        }
        return false;
    }

    // Static methods to test if potential or existing teleporters work
    private static TestableBlock fromPortalArrangement[] = {
        // Center dragon head
        new TestableBlock(0, 0, 0, Material.DRAGON_WALL_HEAD),
        // Obsidian
        new TestableBlock(-1, 0, 0, Material.OBSIDIAN),
        // End stone bricks
        new TestableBlock(-1, 0, -1, Material.END_STONE_BRICKS),
        new TestableBlock(-1, 1, 0,  Material.END_STONE_BRICKS),
        new TestableBlock(-1, 0, 1,  Material.END_STONE_BRICKS),
        new TestableBlock(-1, -1, 0, Material.END_STONE_BRICKS),
        // Purpur corners
        new TestableBlock(-1, 1, -1,  Material.PURPUR_PILLAR),
        new TestableBlock(-1, 1, 1,   Material.PURPUR_PILLAR),
        new TestableBlock(-1, -1, 1,  Material.PURPUR_PILLAR),
        new TestableBlock(-1, -1, -1, Material.PURPUR_PILLAR),
        // Purpur stairs
        new TestableBlock(0, 1, -1,  Material.PURPUR_STAIRS),
        new TestableBlock(0, 1, 1,   Material.PURPUR_STAIRS),
        new TestableBlock(0, -1, 1,  Material.PURPUR_STAIRS),
        new TestableBlock(0, -1, -1, Material.PURPUR_STAIRS),
        // End Rods
        new TestableBlock(0, 0, -1, Material.END_ROD),
        new TestableBlock(0, 1, 0,  Material.END_ROD),
        new TestableBlock(0, 0, 1,  Material.END_ROD),
        new TestableBlock(0, -1, 0, Material.END_ROD),
    };

    public static boolean isSenderFunctional(Location from) {
        return isSenderFunctional(from, null);
    }
    public static boolean isSenderFunctional(Location from, Player p) {
        if(from == null) {
            return false;
        }
        // First check if the dragon head exists        
        // if(!checkBlock(from, 0, 0, 0, Material.DRAGON_WALL_HEAD)) {
        if(!fromPortalArrangement[0].test(from, 0, p)) {
            return false;
        }
        // Then find the index of the direction to go
        for(int i = 0; i < 4; i++) {
            // if(checkBlock(from, -1, 0, 0, Material.OBSIDIAN, i)) {
            if(fromPortalArrangement[1].test(from, i, p)) {
                // Found direction, look for everything else
                // Check for the end stone behind
                for(TestableBlock b : fromPortalArrangement) {
                    if(!b.test(from, i, p)) {
                        return false;
                    }
                }
                return true;
            }
        }
        // If direction wasn't found, return false
        return false;
    }

    private static TestableBlock receiverCenter = new TestableBlock(0, -1, 0, Material.OBSIDIAN);
    private static TestableBlock receiverEdge = new TestableBlock(1, -1, 0, Material.PURPUR_STAIRS);
    private static TestableBlock receiverCorner = new TestableBlock(1, -1, 1, Material.PURPUR_PILLAR);

    public static boolean isReceiverFunctional(Location to) {
        return isReceiverFunctional(to, null);
    }
    public static boolean isReceiverFunctional(Location to, Player p) {
        // TODO check if there is a safe spawning location
        if(to == null) {
            return false;
        }
        int score = 0;
        score += (receiverCenter.test(to, 0, p)) ? 1 : 0;
        for(int i = 0; i < 4; i++) {
            score += (receiverEdge.test(to, i, p)) ? 1 : 0;
            score += (receiverCorner.test(to, i, p)) ? 1 : 0;
        }
        return score == 9;
    }

    public static String serializeLocation(Location l) {
        return l.getWorld().getName() + "<" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ">";
    }

    public static Location parseLocation(String s) {
        Pattern p = Pattern.compile("([^<]+)<(-?\\d+),(-?\\d+),(-?\\d+)");
        Matcher m = p.matcher(s);
        if(m.find()) {
            try {
                int x = Integer.parseInt(m.group(2));
                int y = Integer.parseInt(m.group(3));
                int z = Integer.parseInt(m.group(4));
                return new Location(Bukkit.getServer().getWorld(m.group(1)), x, y, z);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        } else {
            System.out.println("[BlinkLink] No match found for regex while parsing location " + s);
        }
        return null;
    }

}
