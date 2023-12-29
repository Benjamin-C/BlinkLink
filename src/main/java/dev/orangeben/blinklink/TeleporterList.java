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
import java.util.zip.GZIPInputStream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import dev.orangeben.blinklink.SerializableTeleporter;

public class TeleporterList {
    
    private class SerializableTeleporterList implements Serializable {
        private static transient final long serialVersionUID = -1691025206524286331L;
        
        ArrayList<String> stp;

        public SerializableTeleporterList(TeleporterList tl) {
            stp = new ArrayList<String>();
            for(int i : tl.getAll().keySet()) {
                SerializableTeleporter t = new SerializableTeleporter(tl.get(i), i);
                t.setID(i);
                stp.add(t.seralize());
            }
        }

        private static TeleporterList deserialize(ArrayList<String> stp) {
            TeleporterList tl = new TeleporterList();
            for(String s : stp) {
                SerializableTeleporter t = SerializableTeleporter.parse(s);
                tl.addInternal(t, t.getID());
            }
            return tl;
        }

        private static String getSaveLocation() {
            String wname = Bukkit.getWorlds().get(0).getName();
            return wname + File.separatorChar + "blinklinks.bl";
        }
        
        public static String getOldSaveLocation() {
            String wname = Bukkit.getWorlds().get(0).getName();
            return wname + File.separatorChar + "teleporters.tp";
        }

        public boolean saveData() {
            try {
                FileOutputStream fileOut = new FileOutputStream(getSaveLocation());
                // GZIPOutputStream gzOut = new GZIPOutputStream(fileOut);
                BukkitObjectOutputStream out = new BukkitObjectOutputStream(fileOut);
                out.writeObject(stp);
                out.close();
                Bukkit.getLogger().info("Saved teleporters to " + getSaveLocation());
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }

        @SuppressWarnings("unchecked")
        public static TeleporterList loadData() {
            try {
                BukkitObjectInputStream in = null;
                boolean wasOldFile = false;
                try {
                    in = new BukkitObjectInputStream(new FileInputStream(getSaveLocation()));
                    Bukkit.getLogger().info("Loaded teleporters from file " + getSaveLocation() + " ... ");
                } catch(FileNotFoundException e) {
                    in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(getOldSaveLocation())));
                    Bukkit.getLogger().info("Loaded teleporters from old file " + getOldSaveLocation() + " ... ");
                    wasOldFile = true;
                }

                Object inobj = in.readObject();
                try {
                    ArrayList<String> stp = (ArrayList<String>) inobj;
                    in.close();
                    TeleporterList tl = SerializableTeleporterList.deserialize(stp);
                    if(wasOldFile) {
                        tl.save();
                    }
                    return tl;
                } catch (ClassCastException e) {
                    try {
                        Bukkit.getLogger().info("Updating old teleport list");
                        ArrayList<String[]> stp = (ArrayList<String[]>) inobj;
                        TeleporterList tl = new TeleporterList();
                        for(String[] s : stp) {
                            Location from = Teleporter.parseLocation(s[0]);
                            Location to = Teleporter.parseLocation(s[1]);
                            if(from != null && to != null) {
                                Teleporter t = new Teleporter(from, to);
                                tl.addInternal(t, tl.getNewID());
                            } else {
                                Bukkit.getLogger().warning("Couldn't load teleporter");
                            }
                        }
                        tl.save();
                        return tl;
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                return null;
            } catch(FileNotFoundException e) {
                Bukkit.getLogger().warning("The teleporter file was not found.");
                return new TeleporterList();
            } catch(ClassNotFoundException e) {
                Bukkit.getLogger().warning("The teleporter file was found but had a class error.");
                return new TeleporterList();
            } catch (IOException e) {
                e.printStackTrace();
                return new TeleporterList();
            }
        }
    }

    private Map<Integer, Teleporter> tpers;
    private int nextID = 1;

    public TeleporterList() {
        tpers = new HashMap<Integer, Teleporter>();
    }
    
    /**
     * Adds a new teleporter
     * 
     * @param t The teleporter to add
     */
    public int add(Teleporter t) {
        int id = getNewID();
        addInternal(t, id);
        save();
        return id;
    }
    private void addInternal(Teleporter t, int id) {
        // Avoid duplicated
        if(tpers.containsKey(id)) {
            Bukkit.getLogger().warning("Tried to add duplicate teleporter ID " + id + ", can't do that.");
            return;
        }
        for(Teleporter q : tpers.values()) {
            if(q.equals(t)) {
                Bukkit.getLogger().warning("Tried to add duplicate teleporter, can't do that.");
                return;
            }
        }
        // Add if not duplicate
        tpers.put(id, t);
        nextID = Math.max(nextID, id+1);
    }

    /** Returns a new ID for a teleporter */
    protected int getNewID() {
        return nextID++;
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
     * 
     * @param b The block at the location the teleporter is teleporting from (the dragon head)
     * @return  The teleporter
     */
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
     * 
     * @param b The block at the location the teleporter is teleporting from (the dragon head)
     * @return  Wether there is a teleporter from that block or not
     */
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
     * 
     * @param b The block at the location the teleporter is teleporting from (the dragon head)
     * @return  Wether there is a functional teleporter from that block or not
     */
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
        for(int i : tpers.keySet()) {
            if(Teleporter.comprareLocations(l, tpers.get(i).getFrom())) {
                tpers.remove(i);
            }
        }
    }

    /**
     * Removes a teleporter starting from a given block
     * 
     * @param b The block at the location the teleporter is teleporting from (the dragon head)
     */
    public void remove(Block b) {
        remove(b.getLocation());
    }

    /**
     * Saves the list of teleporters to the file
     */
    public void save() {
        SerializableTeleporterList stl = new SerializableTeleporterList(this);
        stl.saveData();
    }

    /**
     * Reads a list of teleporters from the file
     * 
     * @return All the teleporters from the file
     */
    public static TeleporterList fromFile() {
        return SerializableTeleporterList.loadData();
    }

}
