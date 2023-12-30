package dev.orangeben.blinklink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class BLListener implements Listener {

    /** List of all blinklinks that exist */
    private BlinkLinkList tl;
    /** Currently open links since the ender pearl landing event doesn't give info about where the pearl landed.
     *  The pearl land event tells you what block was hit, but doesn't let you set the teleport destination.
     *  The player teleport event lets you change where a pearled player teleports to, but doesn't tell you what block the pearl hit.
     *  Both are needed to detect hitting the dragon head and then move the player when they teleport, this is used to communicate between them.  
     */ 
    private static Map<Player, Location> currentTPs;

    public BLListener(BlinkLinkList tl) {
        this.tl = tl;
        currentTPs = new HashMap<Player, Location>();
    }
    
    public static String stringifyLocation(Location l) {
        return l.getWorld().getName() + "(" + Math.floor(l.getX()) + "," + Math.floor(l.getY()) + "," + Math.floor(l.getZ()) + ")";
    }


    // Watch for players teleporting, and move them accordingly if they are using a blinklink.
    @EventHandler()
    public void enderPearlTeleport(PlayerTeleportEvent event) {
        // If the teleport was an ender pearl
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            // Details of the event ready for use later
            Location from = event.getFrom();
            Player p = event.getPlayer();
            // The destination of the teleport would have been determined when the pearl landed in enderPearlLand(), so see if it was.
            // If not, then the pearl was a normal pearl and not a BlinkLink pearl.
            if(currentTPs.containsKey(p)) {
                Location to = currentTPs.get(p);
                if(to != null) {
                    // Add a half a block in X and Z so that the player teleports to the middle of the block and set the direction so the player doesn't turn
                    to = to.clone().add(0.5, 0, 0.5).setDirection(from.getDirection());
                    // If the target dimension is different, we need to send the player there first
                    // Otherwise the player doesn't go there even though the location is the same.
                    if(!to.getWorld().equals(from.getWorld())) {
                        p.teleport(to);
                        // Could add an additional cost here if desired
                    }
                    // Set the destination for the teleport
                    event.setTo(to);
                } else {
                    // If to was null, then the BlinkLink was broken, so the player shouldn't teleport at all
                    event.setCancelled(true);
                }
                // Remove the teleport we just processed from the list of active teleports.
                currentTPs.remove(p);
            }
        }
    }

    // Watch for pearls landing and see if they are for blinklinks
    @EventHandler
    public void enderPearlLand(ProjectileHitEvent e) {
        // If the projectile was an ender pearl
        if(e.getEntity().getType() == EntityType.ENDER_PEARL) {
            // Details of the event ready for use later
            EnderPearl eep = (EnderPearl) e.getEntity();
            ProjectileSource et = eep.getShooter();
            // Can't use a BlinkLink if you're not a player
            if(et instanceof Player) {
                // More details of the event ready for use later
                Player p = (Player) et;
                Block b = e.getHitBlock();
                // If there is a BlinkLink on the list that leaves from the block the pearl hit
                if(tl.check(b.getLocation())) {
                    // Get that BlinkLink
                    BlinkLink t = tl.get(b.getLocation());
                    // If the sender is functional ...
                    if(BlinkLink.isSenderFunctional(t.getFrom(), p)) {
                        // Make sure that the BlinkLink has been linked
                        if(t.getTo() != null) {
                            // If the receiver is functional ...
                            if(BlinkLink.isReceiverFunctional(t.getTo(), p)) {
                                // Note that the user is using a BlinkLink and where it is going to the pearl's teleport can be redirected later
                                currentTPs.put(p, t.getTo());
                            } else {
                                // If the receiver is broken, let the user know
                                p.sendMessage(Messages.RECEIVER_BROKEN);
                                if(BLPlugin.config.getBoolean(ConfigKeys.TP_CANCEL_ON_BROKEN)) {
                                    currentTPs.put(p, null);
                                }
                            }
                        } else {
                            // If it hasn't been linked, tell user they need to link it first
                            p.sendMessage(Messages.RECEIVER_NOT_BUILT);
                            if(BLPlugin.config.getBoolean(ConfigKeys.TP_CANCEL_ON_BROKEN)) {
                                currentTPs.put(p, null);
                            }
                        }
                    } else {
                        // If the sender isn't functional, tell the user so they know they need to fix it
                        p.sendMessage(Messages.SENDER_BROKEN);
                        if(BLPlugin.config.getBoolean(ConfigKeys.TP_CANCEL_ON_BROKEN)) {
                            currentTPs.put(p, null);
                        }
                    }
                }
            }
        }
    }

    // When a BlinkLink is broken by breaking the dragon head
    @EventHandler
    public void playerBreakTeleporterEvent(BlockBreakEvent e) {
        // If the broken block was a dragon head
        if(e.getBlock().getType() == Material.DRAGON_WALL_HEAD) {
            // If the head was a teleporter
            if(tl.check(e.getBlock().getLocation())) {
                // Tell whoever broke it that it was a teleporter
                e.getPlayer().sendMessage(Messages.BROKE_SENDER);
                tl.remove(e.getBlock().getLocation());
            }
        }
    }

    // When a player creates a new BlinkLink
    @EventHandler
    public void playerMakeTeleporterEvent(BlockPlaceEvent e) {
        // If they placed a dragon head
        if(e.getBlock().getType() == Material.DRAGON_WALL_HEAD) {
            // Might be a BlinkLink, let's see!
            Location bl = e.getBlock().getLocation();
            if(BlinkLink.isSenderFunctional(bl, e.getPlayer())) {
                e.getPlayer().sendMessage(Messages.MADE_SENDER);
                e.getPlayer().sendMessage(Messages.MADE_SENDER_INSTRUCTIONS);
                // Create the BlinkLink
                BlinkLink tper = new BlinkLink(bl, null);
                int id = tl.add(tper);
                // New linking stick
                ItemStack newtpstick = new ItemStack(Material.STICK);
                ItemMeta meta = newtpstick.getItemMeta();
                // Set the name
                meta.displayName(Component.text(Strings.TPSTICK_NAME).color(TextColor.fromHexString("258273")));
                // Add enchant glint
                meta.addEnchant(Enchantment.DURABILITY, 1, false);
                // Set the lore text
                List<Component> lore;
                if(!meta.hasLore()) {
                    lore = new ArrayList<Component>();
                } else {
                    lore = meta.lore();
                }
                lore.add(Component.text(bl.getWorld().getName() + "<" + bl.getBlockX() + "," + bl.getBlockY() + "," + bl.getBlockZ() + ">"));
                meta.lore(lore);
                newtpstick.setItemMeta(meta);
                // Hide the enchantment name
                newtpstick.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                // Set the NBT flag to link the stick to a sender
                NBTItem nbti = new NBTItem(newtpstick);
                nbti.setInteger(Strings.TPSTICK_ID_KEY, id);
                // Give the item to a player
                e.getPlayer().getInventory().addItem(nbti.getItem());
            } else if(BLPlugin.config.getBoolean(ConfigKeys.BUILD_MSG_ON_FAILED_START)) {
                // Let the player know that the place they put the dragon head isn't a valid blinklink start
                e.getPlayer().sendMessage(Messages.MADE_INVALID_SENDER);
            }
        }
    }

    // When a player links a blinklink
    @EventHandler
    public void onPlayerLinkTeleporterEvent(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack is = e.getItem();
            // Don't continue if the item stack doesn't exist
            if(is == null) { return; }
            // Don't continue if the item is the wrong type
            if(is.getType() != Material.STICK) { return; }
            ItemMeta im = is.getItemMeta();
            // Don't continue if there is no meta
            if(im == null) { return; }
            // Don't continue if the item doesn't have the right NBT data
            NBTItem ni = new NBTItem(is);
            if(!ni.hasTag(Strings.TPSTICK_ID_KEY)) { return; }
            // Get the location for the BlinkLink which is one block above the one the player clicked on
            Location dest = e.getClickedBlock().getLocation().add(0, 1, 0);
            Player p = e.getPlayer();
            // If this is a valid receiving structure
            if(BlinkLink.isReceiverFunctional(dest, p)) {
                // Get the link ID and blinklink
                int id = ni.getInteger(Strings.TPSTICK_ID_KEY);
                BlinkLink t = tl.get(id);
                if(t != null) {
                    if(BlinkLink.isSenderFunctional(t.getFrom(), p)) {
                        // Set the destination of the BlinkLink
                        tl.updateTo(id, dest);
                        // Alert the user
                        p.sendMessage(Messages.LINK_CREATED);
                        // Remove their stick
                        is.setAmount(0);
                        BlinkLink.isReceiverFunctional(tl.get(id).getTo(), p);
                    } else {
                        p.sendMessage(Messages.SENDER_NONFUNCTIONAL);
                    }
                } else {
                    p.sendMessage(Messages.SENDER_REMOVED);
                    // Remove their stick if the sender no longer exists
                    is.setAmount(0);
                }
            } else {
                p.sendMessage(Messages.INVALID_PAD);
            }
        }
    }
}
