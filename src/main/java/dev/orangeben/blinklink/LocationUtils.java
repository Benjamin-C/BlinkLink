package dev.orangeben.blinklink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {

    /**
     * Compares two {@link org.bukkit.Location#Location Location} by their world, and block X,Y,&Z components.
     * 
     * @param a The {@link org.bukkit.Location#Location Location} to compare
     * @param b The {@link org.bukkit.Location#Location Location} to compare to
     * @return If they are the same block XYZ place in the same world 
     */
    public static boolean compare(Location a, Location b) {
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

    public interface JSONParser {
        /**
         * Parses a key/value pair from a JSON string
         * @param key   the key string
         * @param value the value string
         */
        public void parsePair(String key, String value);
    }

    /**
     * Parses the next level of SON
     * @param s the JSON string to parse
     * @param p the callback to run on each key/value pair
     */
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

    /**
     * Serializes a {@link org.bukkit.Location#Location Location} to a JSON string. Only stores the floored XYZ values and the string name of the world
     * @param l The {@link org.bukkit.Location#Location Location} to store
     * @return  The JSON string of that
     */
    public static String serialize(Location l) {
        if(l != null) {
            return String.format("{\"world\":\"%s\",\"x\":%d,\"y\":%d,\"z\":%d}", l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
        } else {
            return "null";
        }
    }

    /**
     * Parses a JSON string into a {@link org.bukkit.Location#Location Location}
     * @param j the JSON string
     * @return  the {@link org.bukkit.Location#Location Location} represented by that string
     */
    public static Location deserialize(String j) {
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

    /**
     * Turns a {@link org.bukkit.Location#Location Location} into a custom string in the format worldname<X,Y,Z>
     * @deprecated Use {@link #serialize(Location)} to serialize into JSON
     * @param l The {@link org.bukkit.Location#Location Location}
     * @return  The custom string
     */
    @Deprecated
    public static String serializeLocation(Location l) {
        return l.getWorld().getName() + "<" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ">";
    }

    /**
     * Turns a custom string in the format worldname<X,Y,Z> into a {@link org.bukkit.Location#Location Location}
     * @deprecated Use {@link #serialize(Location)} to serialize into JSON
     * @param l The custom string
     * @return  The {@link org.bukkit.Location#Location Location}
     */
    @Deprecated
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
