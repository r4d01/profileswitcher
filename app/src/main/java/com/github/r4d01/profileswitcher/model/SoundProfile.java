package com.github.r4d01.profileswitcher.model;

import com.github.r4d01.profileswitcher.R;

/**
 * Represents the available sound profiles in the system.
 */
public enum SoundProfile {
    NORMAL(R.string.profile_normal_icon, 1),
    VIBRATION(R.string.profile_vibration_icon, 2),
    DND(R.string.profile_dnd_icon, 3);

    private static final long BUZZ_MS = 60;
    private static final long PAUSE_MS = 80;

    /** Emoji icon shown in the confirmation toast. */
    public final int iconRes;
    /** Number of buzzes confirming the switch to this profile. */
    public final int buzzCount;

    SoundProfile(int iconRes, int buzzCount) {
        this.iconRes = iconRes;
        this.buzzCount = buzzCount;
    }

    /**
     * Vibration waveform timings (off/on/off/on...) for the confirmation buzzes,
     * e.g. {0, 60, 80, 60} for two buzzes.
     */
    public long[] vibrationTimings() {
        long[] timings = new long[buzzCount * 2];
        for (int i = 0; i < buzzCount; i++) {
            timings[i * 2] = i == 0 ? 0 : PAUSE_MS;
            timings[i * 2 + 1] = BUZZ_MS;
        }
        return timings;
    }

    /**
     * Helper method to cycle to the next profile:
     * NORMAL -> VIBRATION -> DND -> NORMAL.
     *
     * @return The next SoundProfile in the cycle.
     */
    public SoundProfile next() {
        switch (this) {
            case NORMAL:
                return VIBRATION;
            case VIBRATION:
                return DND;
            case DND:
            default:
                return NORMAL;
        }
    }
}
