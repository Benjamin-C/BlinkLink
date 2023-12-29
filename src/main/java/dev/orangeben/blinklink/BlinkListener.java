package dev.orangeben.blinklink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public class BlinkListener implements Listener {

    private TeleporterList tl;
    private static Map<Player, Location> currentTPs;
    private static Random random = new Random();

    public BlinkListener(TeleporterList tl) {
        this.tl = tl;
        currentTPs = new HashMap<Player, Location>();
    }
    
    public static String stringifyLocation(Location l) {
        return l.getWorld().getName() + "(" + Math.floor(l.getX()) + "," + Math.floor(l.getY()) + "," + Math.floor(l.getZ()) + ")";
    }


    // Listen for ender pearls
    @EventHandler()
    public void enderPearlTeleport(PlayerTeleportEvent event) {
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            // Location to = event.getTo();
            Location from = event.getFrom();
            Player p = event.getPlayer();
            if(currentTPs.containsKey(p)) {
                Location to = currentTPs.get(p);
                if(to != null) {
                    to.setDirection(from.getDirection());
                    // If the target dimension is different, we need to send the player there first
                    // Otherwise the player doesn't go there even though the location is the same.
                    if(!to.getWorld().equals(from.getWorld())) {
                        p.teleport(to);
                        // Could add an additional cost here if desired
                    }
                    // Set the destination for the teleport
                    event.setTo(to);
                } else {
                    event.setCancelled(true);
                }
                currentTPs.remove(p);
            }
        }
    }

    @EventHandler
    public void enderPearlLand(ProjectileHitEvent e) {
        if(e.getEntity().getType() == EntityType.ENDER_PEARL) {
            EnderPearl eep = (EnderPearl) e.getEntity();
            ProjectileSource et = eep.getShooter();
            if(et instanceof Player) {
                Player p = (Player) et;
                Block b = e.getHitBlock();
                if(tl.check(b)) {
                    Teleporter t = tl.get(b);
                    if(Teleporter.isSenderFunctional(t.getFrom(), p)) {
                        if(Teleporter.isReceiverFunctional(t.getTo(), p)) {
                            currentTPs.put(p, t.getTo());
                        } else {
                            p.sendMessage("The receiver is broken. You must fix it before you can teleport.");
                            if(BlinkLink.config.getBoolean(ConfigKeys.TP_CANCEL_ON_BROKEN)) {
                                currentTPs.put(p, null);
                            }
                        }
                    } else {
                        p.sendMessage("This teleporter is broken." + ((BlinkLink.config.getBoolean(ConfigKeys.TP_DEBUG_INFO)) ?  " " + stringifyLocation(b.getLocation()) + " " + b.getType() : ""));
                        if(BlinkLink.config.getBoolean(ConfigKeys.TP_CANCEL_ON_BROKEN)) {
                            currentTPs.put(p, null);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerBreakTeleporterEvent(BlockBreakEvent e) {
        if(tl.check(e.getBlock())) {
            e.getPlayer().sendMessage("You just broke a teleporter.");
            tl.remove(e.getBlock());
        }
    }

    @EventHandler
    public void playerMakeTeleporterEvent(BlockPlaceEvent e) {
        if(e.getBlock().getType() == Material.DRAGON_WALL_HEAD) {
            // e.getPlayer().sendMessage("Head");
            // Might be a teleporter, let's see!
            Location bl = e.getBlock().getLocation();
            if(Teleporter.isSenderFunctional(bl, e.getPlayer())) {
                e.getPlayer().sendMessage("You just made a teleporter!");
                e.getPlayer().sendMessage("Click on the obsidian of the landing pad to link this teleporter to it.");
                // Generate teleporter ID
                int tpid = random.nextInt();
                // Create the teleporter
                Teleporter tper = new Teleporter(bl, null);
                // New linking stick
                ItemStack newtpstick = new ItemStack(Material.STICK);
                ItemMeta meta = newtpstick.getItemMeta();
                // Set the namewsws
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
                lore.add(Component.text(Teleporter.serializeLocation(bl)));
                meta.lore(lore);
                newtpstick.setItemMeta(meta);
                // Hide the enchantment name
                newtpstick.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                NBTItem nbti = new NBTItem(newtpstick);
                nbti.setInteger(Strings.TPSTICK_ID_KEY, tpid);
                e.getPlayer().getInventory().addItem(nbti.getItem());
                // e.getPlayer().sendMessage("New stick for you!");
            } else if(BlinkLink.config.getBoolean(ConfigKeys.BUILD_MSG_ON_FAILED_START)){
                e.getPlayer().sendMessage("That's not a valid teleporter.");
            }
        }
    }

    @EventHandler
    public void onPlayerInitializeTeleporterEvent(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack is = e.getItem();
            if(is == null) { return; } // Don't continue if the item stack doesn't exist
            ItemMeta im = is.getItemMeta();
            if(im == null) { return; } // Don't continue if there is no meta
            if(im.displayName() == null) { return; } // Don't continue if there is no display name
            if(((TextComponent) im.displayName()).content().equals(Strings.TPSTICK_NAME)) {
                Location dest = e.getClickedBlock().getLocation().add(0, 1, 0);
                Player p = e.getPlayer();
                if(Teleporter.isReceiverFunctional(dest, p)) {
                    // e.getPlayer().sendMessage("You are trying to make a teleporter to " + stringifyLocation(dest));
                    String source = ((TextComponent) im.lore().get(0)).content();
                    Location l = Teleporter.parseLocation(source);
                    if(Teleporter.isSenderFunctional(l, p)) {
                        p.sendMessage("Teleporter created");
                        // TODO fix this
                        Teleporter t = new Teleporter(l, dest);
                        tl.add(t);
                        // p.sendMessage(t.seralize());
                        Teleporter.parse(t.seralize());
                        is.setAmount(0);
                    } else {
                        p.sendMessage("The sender is not functional. Fix it before you can create the link.");
                    }
                } else {
                    p.sendMessage("That isn't a valid teleporter pad");
                }
            }
        }
    }

}
