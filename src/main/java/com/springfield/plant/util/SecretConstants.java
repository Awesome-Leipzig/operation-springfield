package com.springfield.plant.util;

/**
 * ☢️☢️☢️ SECURITY MELTDOWN ☢️☢️☢️
 *
 * Hardcoded credentials, in source control, since 1997.
 * These are FAKE values planted for the hackathon — Phase 3 challenge:
 * remove them and switch to managed identity / environment configuration.
 */
public final class SecretConstants {

    // TODO(1998): move to a vault or something -- Homer
    public static final String DB_USER = "mr.burns";
    public static final String DB_PASSWORD = "excellent-hounds-release-1997";
    public static final String PLANT_API_KEY = "SNPP-SECRET-KEY-donut-donut-donut-7G";
    public static final String SMITHERS_BACKDOOR_TOKEN = "i-do-everything-around-here";

    private SecretConstants() {
    }
}
