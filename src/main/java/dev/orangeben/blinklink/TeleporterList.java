package dev.orangeben.blinklink;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

/**
 * A list of teleporters in the server. Can be saved to a file.
 */
public class TeleporterList implements Serializable {

    /** The teleporters this list contains */
    private Map<Integer, Teleporter> tpers;
    /** Randomizer to generate new IDs */
    private transient Random randomizer = null;

    public TeleporterList() {
        tpers = new HashMap<Integer, Teleporter>();
    }
    
    /**
     * Adds a new teleporter with a newly generated ID and saves the teleporter list.
     * 
     * @param t The teleporter to add
     * @return  The ID of the teleporter
     */
    public int add(Teleporter t) {
        int id = getNewID();
        addInternal(t, id);
        save();
        return id;
    }
    /**
     * Adds a new teleporter. Doesn't add a teleporter with the same ID or the same source/dest as an existing ones.
     * @param t  The teleporter to add
     * @param id The ID of the teleporter.
     * @return   Wether the teleporter was added
     */
    private boolean addInternal(Teleporter t, int id) {
        // Avoid duplicated
        if(tpers.containsKey(id)) {
            Bukkit.getLogger().warning("Tried to add duplicate teleporter ID " + id + ", can't do that.");
            return false;
        }
        for(Teleporter q : tpers.values()) {
            if(q.equals(t)) {
                Bukkit.getLogger().warning("Tried to add duplicate teleporter, can't do that.");
                return false;
            }
        }
        // Add if not duplicate
        tpers.put(id, t);
        return true;
    }

    /**
     * Returns a new ID for a teleporter. Generates IDs randomly then checks if they exist.
     * The ID is guaranteed to be unique at time of generation, but it is not reserved until a teleporter with that ID is added.
     * Use the {@link #add add()} method to 
     */
    protected int getNewID() {
        if(randomizer == null) {
            randomizer = new Random();
        }
        int id = 0;
        do {
            id = randomizer.nextInt();
        } while(tpers.containsKey(id));
        return id;
    }

    /**
     * Gets a teleporter from a given location
     * 
     * @param l The location the teleporter is teleporting from
     * @return  The teleporter
     */
    public Teleporter get(Location l) {
        for(Teleporter t : tpers.values()) {
            if(t.getFrom().getBlockX() == l.getBlockX() && t.getFrom().getBlockY() == l.getBlockY() && t.getFrom().getBlockZ() == l.getBlockZ()) {
                return t;
            }
        }
        return null;
    }
    /**
     * Gets a teleporter from a given block
     * @Deprecated Use {@link #get(Location) get(Location)}
     * 
     * @param b The block at the location the teleporter is teleporting from (the dragon head)
     * @return  The teleporter
     */
    @Deprecated
    public Teleporter get(Block b) {
        return get(b.getLocation());
    }
    /**
     *  Gets a teleporter by its ID
     * 
     * @param id the ID of the desired teleporter
     * @return   The teleporter if it exists, null otherwise
     */
    public Teleporter get(int id) {
        if(tpers.containsKey(id)) {
            return tpers.get(id);
        } else {
            return null;
        }
    }

    /**
     * Checks if a teleporter is registered from a given location
     * 
     * @param l The location the teleporter is teleporting from
     * @return  Wether there is a teleporter from that location or not
     */
    public boolean check(Location l) {
        return get(l) != null;
    }
    /**
     * Checks if a teleporter is registered from a given location
     * @Deprecated Use {@link #check(Location) check(Location)}
     * @param b The block at the location the teleporter is teleporting from (the dragon head)
     * @return  Wether there is a teleporter from that block or not
     */
    @Deprecated
    public boolean check(Block b) {
        return b != null && get(b.getLocation()) != null;
    }

    /**
     * Checks if a teleporter is functional from a given location
     * 
     * @param l The location the teleporter is teleporting from
     * @return  Wether there is a functional teleporter from that location or not
     */
    public boolean checkLink(Location l) {
        Teleporter t = get(l);
        return t != null && t.isOperational();
    }
    /**
     * Checks if a teleporter is functional from a given location
     * @Deprecated Use {@link #checkLink(Location) checkLink(Location)}
     * @param b The block at the location the teleporter is teleporting from (the dragon head)
     * @return  Wether there is a functional teleporter from that block or not
     */
    @Deprecated
    public boolean checkLink(Block b) {
        Teleporter t = get(b);
        return t != null && t.isOperational();
    }

    /**
     * Gets all of the known teleporters
     * 
     * @return All the teleporters
     */
    public Map<Integer, Teleporter> getAll() {
        return tpers;
    }

    /**
     * Gets the number of teleporters on the list
     * 
     * @return The number of teleporters
     */
    public int size() {
        return tpers.size();
    }

    /**
     * Updates the to position of a teleporter and saves the list.
     * @param from The location the teleporter is teleporting from (the dragon head)
     * @param to   The location the teleporter is teleporting to (the air above the obsidian)
     */
    public void updateTo(Location from, Location to) {
        Teleporter t = get(from);
        if(t != null) {
            t.setTo(to);
            save();
        }
    }
    /**
     * Updates the to position of a teleporter and saves the list.
     * @param from The id of the teleporter to modify
     * @param to   The location the teleporter is teleporting to (the air above the obsidian)
     */
    public void updateTo(int from, Location to) {
        Teleporter t = get(from);
        if(t != null) {
            t.setTo(to.clone().add(0.5, 0, 0.5));
            save();
        }
    }

    /**
     * Removes a teleporter starting from a given location
     * 
     * @param l The location the teleporter is teleporting from
     */
    public void remove(Location l) {
        List<Integer> tr = new ArrayList<Integer>();
        for(int i : tpers.keySet()) {
            if(LocationUtils.comprareLocations(l, tpers.get(i).getFrom())) {
                // tpers.remove(i);
                tr.add(i);
            }
        }
        for(int i : tr) {
            tpers.remove(i);
        }
    }

    /**
     * Gets the file location where the list is saved to
     * 
     * @return The file path
     */
    private static String getSaveLocation() {
        String wname = Bukkit.getWorlds().get(0).getName();
        return wname + File.separatorChar + "blinklink.bl";
    }

    /**
     * Removes a teleporter starting from a given block
     * @Deprecated use {@link #remove() remove(Location)}
     * @param b The block at the location the teleporter is teleporting from (the dragon head)
     */
    @Deprecated
    public void remove(Block b) {
        remove(b.getLocation());
    }

    /**
     * Saves the list of teleporters to the file
     */
    public void save() {
        // SerializableTeleporterList stl = new SerializableTeleporterList(this);
        // stl.saveData();
        try {
            FileOutputStream fileOut = new FileOutputStream(getSaveLocation());
            // GZIPOutputStream gzOut = new GZIPOutputStream(fileOut);
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            Bukkit.getLogger().info("Saved teleporters to " + getSaveLocation());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save teleporters");
            e.printStackTrace();
        }
    }

    /**
     * Reads a list of teleporters from the file
     * 
     * @return All the teleporters from the file
     */
    public static TeleporterList fromFile() {
        // return SerializableTeleporterList.loadData();
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new FileInputStream(getSaveLocation()));
            
            TeleporterList tpl = (TeleporterList) in.readObject();
            in.close();

            return tpl;
        } catch(FileNotFoundException e) {
            Bukkit.getLogger().warning("The teleporter file was not found. A new one has been created.");
            TeleporterList tl = new TeleporterList();
            tl.save();
            return tl;
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "The teleporter file was found but had an error. A new one has been created.", e);
            TeleporterList tl = new TeleporterList();
            tl.save();
            return tl;
        }
    }

}
