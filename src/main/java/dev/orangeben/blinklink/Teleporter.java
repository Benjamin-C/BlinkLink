package dev.orangeben.blinklink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Teleporter {
    
    private Location from= null;
    private Location to = null;
    private int id = 0;

    public Teleporter(Location from, Location to) {
        this.from = from.clone();
        if(to != null) {
            this.to = to.clone().add(0.5, 0, 0.5);
        }
    }
    private Teleporter() {
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
        this.from = l;
    }

    /**
     * Sets the location the teleporter teleports from. This is the air just above the obsidian
     * 
     * @param l The location
     */
    public void setTo(Location l) {
        this.to = l;
    }

    /**
     * Gets the ID of the teleporter
     * @return The ID
     */
    public int getID() {
        return id;
    }

    /**
     * Sets the ID of the teleporter. DO NOT DO THIS if you are using a teleporter list
     * @param newID the new ID
     */
    public void setID(int newID) {
        this.id = newID;
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
            return comprareLocations(this.from, that.from) && comprareLocations(this.to, that.to);
            // return this.from.getWorld().equals(that.from.getWorld()) && 
            //     this.from.getBlockX() == that.from.getBlockX() && 
            //     this.from.getBlockY() == that.from.getBlockY() && 
            //     this.from.getBlockZ() == that.from.getBlockZ() && 
            //     (
            //         (this.to == null && that.to == null) ||
            //         (this.to != null && that.to != null &&
            //         this.to.getBlockX() == that.to.getBlockX() &&
            //         this.to.getBlockY() == that.to.getBlockY() &&
            //         this.to.getBlockZ() == that.to.getBlockZ())
            //     );
                
        }
        return false;
    }

    public static boolean comprareLocations(Location a, Location b) {
        return
            (a == null && b == null) // Both are null
            ||
            ( // If Both are not null, then the blocks must match
                a != null && b != null &&
                a.getWorld().equals(b.getWorld()) &&
                a.getBlockX() == b.getBlockX() &&
                a.getBlockY() == b.getBlockY() &&
                a.getBlockZ() == b.getBlockZ()
            );
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
        for(int x = -1; x <= 1; x++) {
            for(int y = 0; y <= 1; y++) {
                for(int z = -1; z <= 1; z++) {
                    score += (to.clone().add(x, y, z).getBlock().isSolid()) ? 0 : 1;
                }
            }
        }
        return score == 27;
    }

    public String seralize() {
        return "{\"from\":" + jsonizeLocation(from) + ",\"to\":" + jsonizeLocation(to) + ",\"id\":" + id + "}";
    }

    private interface JSONParser {
        public void parsePair(String key, String value);
    }

    public static void parseJSONLevel(String s, JSONParser p) {
        Pattern ptn = Pattern.compile("(?:\\\"(\\w+)\\\"):(?:(?:\\\"(\\w+)\\\")|(\\{[^\\}]*\\})|(\\d+))");
        // From regexer (?:\"(\w+)\"):(?:(?:\"(\w+)\")|(?:\{([^\}]*)})|(\d+))
        Matcher m = ptn.matcher(s);
        while(m.find() && m.groupCount() >= 3) {
            String key = m.group(1);
            String value = "";
            for(int i = 2; i < m.groupCount()+1; i++) {
                if(m.group(i) != null) {
                    value = m.group(i);
                }
            }
            p.parsePair(key, value);
        }
    }

    public static Teleporter parse(String s) {
        Teleporter t = new Teleporter();

        parseJSONLevel(s, (key, value) -> {
            switch(key) {
                case "to": {
                    if(t.to == null) {
                        t.to = unjasonizeLocation(value).add(0.5, 0, 0.5);
                    } else {
                        Bukkit.getLogger().warning("Duplicate to field while parsing teleporter from JSON " + s);
                    }
                } break;
                case "from": {
                    if(t.from == null) {
                        t.from = unjasonizeLocation(value);
                    } else {
                        Bukkit.getLogger().warning("Duplicate from field while parsing teleporter from JSON " + s);
                    }
                } break;
                case "id": {
                    if(t.id == 0) {
                        try {
                            t.id = Integer.parseInt(value);
                        } catch (Exception e) {
                            Bukkit.getLogger().severe("NumberFormatException while parsing teleportor ID from JSON " + s);
                        }
                    } else {
                        Bukkit.getLogger().warning("Duplicate id field while parsing teleporter from JSON " + s);
                    }
                } break;
                default: {
                    Bukkit.getLogger().warning("Unknown key " + key + "=" + value + " found while parsing Teleporter.");
                } break;
            }
        });
        return t;
    }

    public static String jsonizeLocation(Location l) {
        if(l != null) {
            return String.format("{\"world\":\"%s\",\"x\":%d,\"y\":%d,\"z\":%d}", l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
        } else {
            return "null";
        }
    }

    public static Location unjasonizeLocation(String j) {
        if(j.equals("null")) {
            return null;
        }
        var wrapper = new Object() {
            int x, y, z;
            String world = "world";
        };
        parseJSONLevel(j, (key, value) -> {
            switch(key) {
                case "x": {
                    try {
                        wrapper.x = Integer.parseInt(value);
                    } catch (Exception e) {
                        Bukkit.getLogger().severe("NumberFormatException while parsing location from JSON " + j);
                    }
                } break;
                case "y": {
                    try {
                        wrapper.y = Integer.parseInt(value);
                    } catch (Exception e) {
                        Bukkit.getLogger().severe("NumberFormatException while parsing location from JSON " + j);
                    }
                } break;
                case "z": {
                    try {
                        wrapper.z = Integer.parseInt(value);
                    } catch (Exception e) {
                        Bukkit.getLogger().severe("NumberFormatException while parsing location from JSON " + j);
                    }
                } break;
                case "world": {
                    wrapper.world = value;
                } break;
                default: {
                    Bukkit.getLogger().warning("Unknown key " + key + "=" + value + " found while parsing Teleporter.");
                } break;
            }
        });
        return new Location(Bukkit.getWorld(wrapper.world), wrapper.x, wrapper.y, wrapper.z);
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
