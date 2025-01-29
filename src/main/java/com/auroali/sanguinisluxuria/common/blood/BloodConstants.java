package com.auroali.sanguinisluxuria.common.blood;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public class BloodConstants {
    public static final int BLOOD_PER_BOTTLE = 2;
    public static final int BLOOD_GAIN_RATE = 4800;
    public static final int BLOOD_DRAIN_TIME = 15;
    public static final int BLOOD_DRAIN_TIME_BLEEDING = 10;

    /**
     * Converts blood units to droplets
     *
     * @param blood the amount of blood
     * @return the amount of droplets
     */
    @SuppressWarnings("UnstableApiUsage")
    public static long bloodToDroplets(int blood) {
        return blood * FluidConstants.BOTTLE / BloodConstants.BLOOD_PER_BOTTLE;
    }

    /**
     * Converts droplets to blood units
     *
     * @param droplets the amount of droplets
     * @return the amount of blood, rounded down
     */
    @SuppressWarnings("UnstableApiUsage")
    public static int dropletsToBlood(long droplets) {
        return (int) (droplets * BloodConstants.BLOOD_PER_BOTTLE / FluidConstants.BOTTLE);
    }

    public static int bottles(int num) {
        return BLOOD_PER_BOTTLE * num;
    }
}
