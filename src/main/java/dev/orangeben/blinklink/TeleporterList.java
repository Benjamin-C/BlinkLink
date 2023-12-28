package dev.orangeben.blinklink;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class TeleporterList {
    
    private class SerializableTeleporterList implements Serializable {
        private static transient final long serialVersionUID = -1691025206524286331L;
        
        ArrayList<String[]> stp;

        public SerializableTeleporterList(TeleporterList tl) {
            stp = new ArrayList<String[]>();
            for(Teleporter t : tl.getAll()) {
                String s[] = {
                    Teleporter.serializeLocation(t.getFrom()),
                    Teleporter.serializeLocation(t.getTo())
                };
                stp.add(s);
            }
        }

        private static TeleporterList deserialize(ArrayList<String[]> stp) {
            TeleporterList tl = new TeleporterList();
            for(String[] s : stp) {
                Location from = Teleporter.parseLocation(s[0]);
                Location to = Teleporter.parseLocation(s[1]);
                if(from != null && to != null) {
                    tl.addInternal(new Teleporter(from, to));
                } else {
                    Bukkit.getLogger().warning("Couldn't load teleporter");
                }
            }
            return tl;
        }

        private static String getSaveLocation() {
            String wname = Bukkit.getWorlds().get(0).getName();
            return wname + File.separatorChar + "teleporters.tp";
        }


        public boolean saveData() {
            try {
                FileOutputStream fileOut = new FileOutputStream(getSaveLocation());
                GZIPOutputStream gzOut = new GZIPOutputStream(fileOut);
                BukkitObjectOutputStream out = new BukkitObjectOutputStream(gzOut);
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
                Bukkit.getLogger().info("Loaded teleporters from file " + getSaveLocation() + " ... ");
                BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(getSaveLocation())));
                ArrayList<String[]> stp = (ArrayList<String[]>) in.readObject();
                in.close();
                return SerializableTeleporterList.deserialize(stp);
            } catch(FileNotFoundException e) {
                Bukkit.getLogger().warning("The teleporter file was not found.");
                return new TeleporterList();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
                return new TeleporterList();
            }
        }
    }

    private List<Teleporter> tpers;

    public TeleporterList() {
        tpers = new ArrayList<Teleporter>();
    }
    
    /**
     * Adds a new teleporter
     * 
     * @param t The teleporter to add
     */
    public void add(Teleporter t) {
        addInternal(t);
        save();
    }
    private void addInternal(Teleporter t) {
        // Avoid duplicated
        for(Teleporter q : tpers) {
            if(q.equals(t)) {
                Bukkit.getLogger().warning("Tried to add duplicate teleporter, can't do that.");
                return;
            }
        }
        // Add if not duplicate
        tpers.add(t);
    }

    /**
     * Gets a teleporter from a given location
     * 
     * @param l The location the teleporter is teleporting from
     * @return  The teleporter
     */
    public Teleporter get(Location l) {
        for(Teleporter t : tpers) {
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
    public List<Teleporter> getAll() {
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
     * Removes a teleporter starting from a given location
     * 
     * @param l The location the teleporter is teleporting from
     */
    public void remove(Location l) {
        tpers.remove(get(l));
        save();
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
