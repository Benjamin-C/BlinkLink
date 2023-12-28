package dev.orangeben.blinklink;

public class ConfigKeys {
    
    public static final String TEST_BASE = "TestableBlock";
    public static final String TEST_BROADCAST = TEST_BASE + ".broadcast";
    public static final String TEST_MESSAGE = TEST_BASE + ".message";
    public static final String TEST_PARTICLE = TEST_BASE + ".particle";

    public static final String TP_BASE = "Teleport";
    public static final String TP_CANCEL_ON_BROKEN = TP_BASE + ".cancelonbroken";
    public static final String TP_DEBUG_INFO = TP_BASE + ".debuginfo";

    public static final String BUILD_BASE = "Build";
    public static final String BUILD_MSG_ON_FAILED_START = BUILD_BASE + ".msgonbrokenstart";

}
