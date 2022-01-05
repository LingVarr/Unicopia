package com.minelittlepony.unicopia.util;

import net.minecraft.util.math.MathHelper;

public interface AnimationUtil {
    /**
     * Converts a smooth 0-1 transition to a smooth 0-1-0 transition
     */
    static float seesaw(float progress) {
        return Math.max(0, MathHelper.cos((progress - 0.5F) * (float)Math.PI));
    }

    /**
     * Converts a smooth 0-1 transition to a stretched 0-1-1-1-1-0 transition
     */
    static float seeSitSaw(float progress, float clipRatio) {
        return Math.min(1, seesaw(progress) * clipRatio);
    }
}
