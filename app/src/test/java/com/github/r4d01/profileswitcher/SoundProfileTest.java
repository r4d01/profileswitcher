package com.github.r4d01.profileswitcher;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.github.r4d01.profileswitcher.model.SoundProfile;

import org.junit.Test;

public class SoundProfileTest {

    @Test
    public void testProfileCycling() {
        SoundProfile profile = SoundProfile.NORMAL;

        profile = profile.next();
        assertEquals(SoundProfile.VIBRATION, profile);

        profile = profile.next();
        assertEquals(SoundProfile.DND, profile);

        profile = profile.next();
        assertEquals(SoundProfile.NORMAL, profile);
    }

    @Test
    public void testVibrationTimings() {
        assertArrayEquals(new long[]{0, 60}, SoundProfile.NORMAL.vibrationTimings());
        assertArrayEquals(new long[]{0, 60, 80, 60}, SoundProfile.VIBRATION.vibrationTimings());
        assertArrayEquals(new long[]{0, 60, 80, 60, 80, 60}, SoundProfile.DND.vibrationTimings());
    }
}
