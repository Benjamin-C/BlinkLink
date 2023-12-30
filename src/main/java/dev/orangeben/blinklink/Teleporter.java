package dev.orangeben.blinklink;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import dev.orangeben.blinklink.TestableBlock.TestType;

/**
 * Represents a BlinkLink teleporter.
 * 
 * from is where the teleporter leaves from. It is the dragon head on the sending structure.
 * to is where the teleporter goes to. It is the air above the obsidian on the receiving structure.
 */
public class Teleporter implements Serializable {
    
    /** The location the teleporter is leaving from (the dragon head) */
    protected Location from = null;
    /** The location the teleporter is going to (the air above the obsidian) */
    protected Location to = null;

    public Teleporter(Location from, Location to) {
        setFrom(from);
        setTo(to);
    }
    protected Teleporter() {
        // Do all setup manually yourself! Don't use this unless you know what you're doing!
    }

    /**
     * Gets the location the teleporter teleports from. This is the dragon head.
     * @return The location
     */
    public Location getFrom() {
        return from;
    }

    /**
     * Gets the location the teleporter teleports from. This is the air just above the obsidian
     * @return The Location
     */
    public Location getTo() {
        return to;
    }

    /**
     * Sets the location the teleporter teleports from. This is the dragon head.
     * 
     * @param l The location
     */
    public void setFrom(Location l) {
        this.from = l.clone();
    }

    /**
     * Sets the location the teleporter teleports from. This is the air just above the obsidian
     * 
     * @param l The location
     */
    public void setTo(Location l) {
        if(l != null) {
            this.to = l.clone();
            // this.to = l.clone().add(0.5, 0, 0.5);
        } else {
            this.to = null;
        }
    }

    /**
     * Checks if both the sending and receiving structures exist.
     * 
     * @return If both structures exist.
     */
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
            return LocationUtils.compare(this.from, that.from) && LocationUtils.compare(this.to, that.to);
        }
        return false;
    }

    // From Serializable
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(LocationUtils.serialize(from));
        oos.writeObject(LocationUtils.serialize(to));
    }

    // From Serializable
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        this.setFrom(LocationUtils.deserialize((String) ois.readObject()));
        this.setTo(LocationUtils.deserialize((String) ois.readObject()));
    }

    // Static methods to test if potential or existing teleporters work
    /** Description of the sending structure */
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


    /**
     * Checks if a sending structure at a given location is functional
     * @param from The location of the dragon head
     * @return If that is a valid sending structure
     */
    public static boolean isSenderFunctional(Location from) {
        return isSenderFunctional(from, null);
    }
    /**
     * Checks if a sending structure at a given location is functional
     * @param from The location of the dragon head
     * @param p    The player to show debug messages to if they are enabled in the server config
     * @return If that is a valid sending structure
     */
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
            if(fromPortalArrangement[1].test(from, i, p, true)) {
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

    /**
     * Checks if a receiving structure at a given location is functional
     * @param from The location of the air above the obsidian
     * @param p    The player to show debug messages to if they are enabled in the server config
     * @return If that is a valid receiving structure
     */
    public static boolean isReceiverFunctional(Location to) {
        return isReceiverFunctional(to, null);
    }
    /**
     * Checks if a receiving structure at a given location is functional
     * @param from The location of the air above the obsidian
     * @return If that is a valid receiving structure
     */
    public static boolean isReceiverFunctional(Location to, Player p) {
        if(to == null) {
            return false;
        }
        int score = 0;
        score += (receiverCenter.test(to, 0, p)) ? 1 : 0;
        for(int i = 0; i < 4; i++) {
            score += (receiverEdge.test(to, i, p)) ? 1 : 0;
            score += (receiverCorner.test(to, i, p)) ? 1 : 0;
        }
        for(int x = -1; x <= 1; x++) {
            for(int y = 0; y <= 1; y++) {
                for(int z = -1; z <= 1; z++) {
                    score += ((new TestableBlock(x, y, z, TestType.PASSABLE)).test(to, p)) ? 1 : 0;
                }
            }
        }
        return score == 27;
    }
}
