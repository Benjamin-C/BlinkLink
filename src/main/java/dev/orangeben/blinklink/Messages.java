package dev.orangeben.blinklink;

public class Messages {
    
    public static final String CONFIG_RELOADED = "Reloading config";

    // When trying to teleport
    public static final String RECEIVER_BROKEN = "The landing pad is broken.";
    public static final String SENDER_BROKEN = "This BlinkLink station is broken.";
    public static final String RECEIVER_NOT_LINKED = "No landing pad has been linked to this station.";
    
    // When creating or destroying a link
    public static final String BROKE_SENDER = "You just broke a BlinkLink.";
    public static final String MADE_SENDER = "You just made a BlinkLink station!";
    public static final String LINK_SENDER_INSTRUCTIONS = "Click on the obsidian of the landing pad with the linking stick to link this station to it.";
    public static final String LINK_CREATED = "Link created";

    // Invalid structure errors
    public static final String MADE_INVALID_SENDER = "That's not a valid BlinkLink station.";
    public static final String SENDER_NONFUNCTIONAL = "The station is broken. Fix it before you can create the link.";
    public static final String SENDER_REMOVED = "The station associated with this stick has been removed.";
    public static final String INVALID_PAD = "That isn't a valid landing pad";

}
