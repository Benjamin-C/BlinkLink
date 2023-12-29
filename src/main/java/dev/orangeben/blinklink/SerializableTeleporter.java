package dev.orangeben.blinklink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SerializableTeleporter extends Teleporter {
    int id = 0;

    public SerializableTeleporter(Location from, Location to, int id) {
        super(from, to);
        this.id = id;
    }
    public SerializableTeleporter(Teleporter t, int id) {
        this.to = t.to;
        this.from = t.from;
        this.id = id;
    }
    public SerializableTeleporter() {
        // Do all setup manually yourself! Don't use this unless you know what you're doing!
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

    public static SerializableTeleporter parse(String s) {
        SerializableTeleporter t = new SerializableTeleporter();

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
}