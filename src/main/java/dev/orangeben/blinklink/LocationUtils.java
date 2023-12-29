package dev.orangeben.blinklink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {

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

    public interface JSONParser {
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

    public static String serialize(Location l) {
        if(l != null) {
            return String.format("{\"world\":\"%s\",\"x\":%d,\"y\":%d,\"z\":%d}", l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
        } else {
            return "null";
        }
    }

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

    @Deprecated
    public static String serializeLocation(Location l) {
        return l.getWorld().getName() + "<" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ">";
    }

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
