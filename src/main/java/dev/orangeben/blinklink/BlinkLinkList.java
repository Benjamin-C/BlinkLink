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
 * A list of BlinkLinks in the server. Can be saved to a file.
 */
public class BlinkLinkList implements Serializable {

    /** The BlinkLink this list contains */
    private Map<Integer, BlinkLink> blers;
    /** Randomizer to generate new IDs */
    private transient Random randomizer = null;

    public BlinkLinkList() {
        blers = new HashMap<Integer, BlinkLink>();
    }
    
    /**
     * Adds a new BlinkLink with a newly generated ID and saves the BlinkLink list.
     * 
     * @param t The BlinkLink to add
     * @return  The ID of the BlinkLink
     */
    public int add(BlinkLink t) {
        int id = getNewID();
        addInternal(t, id);
        save();
        return id;
    }
    /**
     * Adds a new BlinkLink. Doesn't add a BlinkLink with the same ID or the same source/dest as an existing ones.
     * @param t  The BlinkLink to add
     * @param id The ID of the BlinkLink.
     * @return   Wether the BlinkLink was added
     */
    private boolean addInternal(BlinkLink t, int id) {
        // Avoid duplicated
        if(blers.containsKey(id)) {
            Bukkit.getLogger().warning("Tried to add duplicate BlinkLink ID " + id + ", can't do that.");
            return false;
        }
        for(BlinkLink q : blers.values()) {
            if(q.equals(t)) {
                Bukkit.getLogger().warning("Tried to add duplicate BlinkLink, can't do that.");
                return false;
            }
        }
        // Add if not duplicate
        blers.put(id, t);
        return true;
    }

    /**
     * Returns a new ID for a BlinkLink. Generates IDs randomly then checks if they exist.
     * The ID is guaranteed to be unique at time of generation, but it is not reserved until a BlinkLink with that ID is added.
     * Use the {@link #add add()} method to 
     */
    protected int getNewID() {
        if(randomizer == null) {
            randomizer = new Random();
        }
        int id = 0;
        do {
            id = randomizer.nextInt();
        } while(blers.containsKey(id));
        return id;
    }

    /**
     * Gets a BlinkLink from a given location
     * 
     * @param l The location the BlinkLink is linking from
     * @return  The BlinkLink
     */
    public BlinkLink get(Location l) {
        for(BlinkLink t : blers.values()) {
            if(t.getFrom().getBlockX() == l.getBlockX() && t.getFrom().getBlockY() == l.getBlockY() && t.getFrom().getBlockZ() == l.getBlockZ()) {
                return t;
            }
        }
        return null;
    }

    /**
     *  Gets a BlinkLink by its ID
     * 
     * @param id the ID of the desired BlinkLink
     * @return   The BlinkLink if it exists, null otherwise
     */
    public BlinkLink get(int id) {
        if(blers.containsKey(id)) {
            return blers.get(id);
        } else {
            return null;
        }
    }

    /**
     * Checks if a BlinkLink is registered from a given location
     * 
     * @param l The location the BlinkLink is linking from
     * @return  Wether there is a BlinkLink from that location or not
     */
    public boolean check(Location l) {
        return get(l) != null;
    }

    /**
     * Checks if a BlinkLink is functional from a given location
     * 
     * @param l The location the BlinkLink is linking from
     * @return  Wether there is a functional BlinkLink from that location or not
     */
    public boolean checkLink(Location l) {
        BlinkLink t = get(l);
        return t != null && t.isOperational();
    }

    /**
     * Gets all of the known BlinkLinks
     * 
     * @return All the BlinkLink
     */
    public Map<Integer, BlinkLink> getAll() {
        return blers;
    }

    /**
     * Gets the number of BlinkLinks on the list
     * 
     * @return The number of BlinkLinks
     */
    public int size() {
        return blers.size();
    }

    /**
     * Updates the to position of a BlinkLink and saves the list.
     * @param from The location the BlinkLink is linking from (the dragon head)
     * @param to   The location the BlinkLink is linking to (the air above the obsidian)
     */
    public void updateTo(Location from, Location to) {
        BlinkLink t = get(from);
        if(t != null) {
            t.setTo(to);
            save();
        }
    }
    /**
     * Updates the to position of a BlinkLink and saves the list.
     * @param from The id of the BlinkLink to modify
     * @param to   The location the BlinkLink is linking to (the air above the obsidian)
     */
    public void updateTo(int from, Location to) {
        BlinkLink t = get(from);
        if(t != null) {
            t.setTo(to.clone().add(0.5, 0, 0.5));
            save();
        }
    }

    /**
     * Removes a BlinkLink starting from a given location
     * 
     * @param l The location the BlinkLink is linking from
     */
    public void remove(Location l) {
        List<Integer> tr = new ArrayList<Integer>();
        for(int i : blers.keySet()) {
            if(LocationUtils.compare(l, blers.get(i).getFrom())) {
                // blers.remove(i);
                tr.add(i);
            }
        }
        for(int i : tr) {
            blers.remove(i);
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
     * Removes a BlinkLink starting from a given block
     * @Deprecated use {@link #remove() remove(Location)}
     * @param b The block at the location the BlinkLink is linking from (the dragon head)
     */
    @Deprecated
    public void remove(Block b) {
        remove(b.getLocation());
    }

    /**
     * Saves the list of BlinkLinks to the file
     */
    public void save() {
        try {
            FileOutputStream fileOut = new FileOutputStream(getSaveLocation());
            // GZIPOutputStream gzOut = new GZIPOutputStream(fileOut);
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            Bukkit.getLogger().info("Saved BlinkLinks to " + getSaveLocation());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save BlinkLinks");
            e.printStackTrace();
        }
    }

    /**
     * Reads a list of BlinkLink from the file
     * 
     * @return All the BlinkLink from the file
     */
    public static BlinkLinkList fromFile() {
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new FileInputStream(getSaveLocation()));
            
            BlinkLinkList tpl = (BlinkLinkList) in.readObject();
            in.close();

            return tpl;
        } catch(FileNotFoundException e) {
            Bukkit.getLogger().warning("The BlinkLink file was not found. A new one has been created.");
            BlinkLinkList tl = new BlinkLinkList();
            tl.save();
            return tl;
        } catch(Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "The BlinkLink file was found but had an error. A new one has been created.", e);
            BlinkLinkList tl = new BlinkLinkList();
            tl.save();
            return tl;
        }
    }

}
